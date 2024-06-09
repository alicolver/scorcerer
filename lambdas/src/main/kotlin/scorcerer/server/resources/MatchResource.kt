package scorcerer.server.resources

import kotlinx.coroutines.runBlocking
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.MatchApi
import org.openapitools.server.models.*
import org.openapitools.server.models.Prediction
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.*
import scorcerer.server.db.tables.MatchRound
import scorcerer.server.log
import scorcerer.utils.LeaderboardS3Service
import scorcerer.utils.MatchResult
import scorcerer.utils.PointsCalculator.calculatePoints
import scorcerer.utils.recalculateLivePoints
import scorcerer.utils.toTitleCase

class MatchResource(
    context: RequestContexts,
    private val leaderboardService: LeaderboardS3Service,
) : MatchApi(context) {
    override fun getMatchPredictions(requesterUserId: String, matchId: String, leagueId: String?): List<Prediction> {
        val matchState = transaction {
            MatchTable.select(MatchTable.state).where { MatchTable.id eq matchId.toInt() }.firstOrNull()
                ?.let { row -> row[MatchTable.state] }
                ?: throw ApiResponseError(Response(Status.NOT_FOUND).body("Match does not exist"))
        }

        if (matchState == State.UPCOMING) {
            throw ApiResponseError(Response(Status.BAD_REQUEST).body("Match does not exist"))
        }

        return transaction {
            if (leagueId.isNullOrBlank()) {
                PredictionTable.selectAll().where { (PredictionTable.matchId eq matchId.toInt()) }
            } else {
                (PredictionTable innerJoin MemberTable innerJoin LeagueMembershipTable).selectAll().where {
                    (PredictionTable.matchId eq matchId.toInt()).and(LeagueMembershipTable.leagueId eq leagueId)
                }
            }.map { row ->
                Prediction(
                    row[PredictionTable.homeScore],
                    row[PredictionTable.awayScore],
                    row[PredictionTable.matchId].toString(),
                    row[PredictionTable.id].toString(),
                    row[PredictionTable.memberId],
                    row[PredictionTable.points],
                )
            }
        }
    }

    override fun listMatches(requesterUserId: String, filterType: String?): List<Match> {
        val matches = transaction {
            val awayTeamTable = TeamTable.alias("awayTeam")
            val homeTeamTable = TeamTable.alias("homeTeam")

            val predictions = PredictionTable.selectAll().where { PredictionTable.memberId eq requesterUserId }.alias("predictions")

            val matchTeamTable =
                MatchTable
                    .join(awayTeamTable, JoinType.INNER, MatchTable.awayTeamId, awayTeamTable[TeamTable.id])
                    .join(homeTeamTable, JoinType.INNER, MatchTable.homeTeamId, homeTeamTable[TeamTable.id])
                    .join(predictions, JoinType.LEFT, MatchTable.id, predictions[PredictionTable.matchId])
                    .selectAll()

            if (filterType.isNullOrBlank()) {
                matchTeamTable
            } else {
                matchTeamTable.where { MatchTable.state eq State.valueOf(filterType.uppercase()) }
            }.map { row ->
                Match(
                    row[homeTeamTable[TeamTable.name]].toTitleCase(),
                    row[homeTeamTable[TeamTable.flagUri]],
                    row[awayTeamTable[TeamTable.name]].toTitleCase(),
                    row[awayTeamTable[TeamTable.flagUri]],
                    row[MatchTable.id].toString(),
                    row[MatchTable.venue],
                    row[MatchTable.datetime],
                    row[MatchTable.matchDay],
                    Round.valueOf(row[MatchTable.round].value),
                    row[MatchTable.homeScore],
                    row[MatchTable.awayScore],
                    row[MatchTable.state],
                    row.getOrNull(predictions[PredictionTable.id])?.let {
                        Prediction(
                            row[predictions[PredictionTable.homeScore]],
                            row[predictions[PredictionTable.awayScore]],
                            row[MatchTable.id].toString(),
                            row[predictions[PredictionTable.id]].toString(),
                            row[predictions[PredictionTable.memberId]],
                            row[predictions[PredictionTable.points]],
                        )
                    },
                )
            }
        }

        if (!filterType.isNullOrBlank() && State.valueOf(filterType.uppercase()) == State.UPCOMING) {
            log.info("Filtering matches to next 2 match days")
            return getMatchesOnNextNMatchDays(matches)
        }

        return matches
    }

    override fun setMatchScore(
        requesterUserId: String,
        matchId: String,
        setMatchScoreRequest: SetMatchScoreRequest,
    ) {
        transaction {
            val matchDay = getMatchDay(matchId)
                ?: throw ApiResponseError(Response(Status.BAD_REQUEST).body("Match does not exist"))
            MatchTable.update({ MatchTable.id eq matchId.toInt() }) {
                it[homeScore] = setMatchScoreRequest.homeScore
                it[awayScore] = setMatchScoreRequest.awayScore
                it[state] = State.LIVE
            }

            val predictions = getPredictions(matchId)

            predictions.forEach { prediction ->
                val points = calculatePoints(
                    prediction,
                    MatchResult(
                        setMatchScoreRequest.homeScore,
                        setMatchScoreRequest.awayScore,
                    ),
                )
                updatePredictionPoints(prediction.predictionId.toInt(), points)
            }
            recalculateLivePoints()
            runBlocking {
                leaderboardService.updateGlobalLeaderboard(matchDay)
            }
        }
    }

    override fun createMatch(
        requesterUserId: String,
        createMatchRequest: CreateMatchRequest,
    ): CreateMatch200Response {
        val id = transaction {
            MatchTable.insert {
                it[this.homeTeamId] = createMatchRequest.homeTeamId.toInt()
                it[this.awayTeamId] = createMatchRequest.awayTeamId.toInt()
                it[this.datetime] = createMatchRequest.datetime
                it[this.state] = State.UPCOMING
                it[this.venue] = createMatchRequest.venue
                it[this.matchDay] = createMatchRequest.matchDay
                it[this.round] = MatchRound.valueOf(createMatchRequest.matchRound.value)
            } get MatchTable.id
        }
        return CreateMatch200Response(id.toString())
    }

    override fun completeMatch(
        requesterUserId: String,
        matchId: String,
        completeMatchRequest: CompleteMatchRequest,
    ) {
        val matchDay = transaction {
            val matchDay = getMatchDay(matchId)
                ?: throw ApiResponseError(Response(Status.BAD_REQUEST).body("Match does not exist"))

            MatchTable.update({ MatchTable.id eq matchId.toInt() }) {
                it[state] = State.COMPLETED
                it[homeScore] = completeMatchRequest.homeScore
                it[awayScore] = completeMatchRequest.awayScore
            }

            val predictions = getPredictions(matchId)

            predictions.forEach { prediction ->
                val points = calculatePoints(
                    prediction,
                    MatchResult(
                        completeMatchRequest.homeScore,
                        completeMatchRequest.awayScore,
                    ),
                )
                updatePredictionPoints(prediction.predictionId.toInt(), points)
                updateMemberFixedPoints(prediction.userId, points)
            }
            matchDay
        }
        recalculateLivePoints()
        runBlocking {
            leaderboardService.updateGlobalLeaderboard(matchDay)
        }
    }
}

fun getMatchesOnNextNMatchDays(matches: List<Match>): List<Match> {
    val uniqueMatchDays = matches.map { it.matchDay }.distinct()
    if (uniqueMatchDays.size < 2) {
        val lowestMatchDay = uniqueMatchDays.minOrNull() ?: return emptyList()
        return matches.filter { it.matchDay == lowestMatchDay }
    }
    val lowestMatchDays = uniqueMatchDays.sorted().take(2)
    return matches.filter { it.matchDay in lowestMatchDays }
}

private fun getMatchDay(matchId: String): Int? {
    return MatchTable.select(MatchTable.matchDay).where { MatchTable.id eq matchId.toInt() }
        .firstOrNull()?.let { row -> row[MatchTable.matchDay] }
}

private fun getPredictions(matchId: String): List<Prediction> {
    return PredictionTable.selectAll().where { PredictionTable.matchId eq matchId.toInt() }.map { row ->
        Prediction(
            row[PredictionTable.homeScore],
            row[PredictionTable.awayScore],
            row[PredictionTable.matchId].toString(),
            row[PredictionTable.id].toString(),
            row[PredictionTable.memberId],
        )
    }
}

private fun updatePredictionPoints(predictionId: Int, points: Int) {
    PredictionTable.update({ PredictionTable.id eq predictionId }) {
        it[PredictionTable.points] = points
    }
}

private fun updateMemberFixedPoints(userId: String, points: Int) {
    MemberTable.update({ MemberTable.id eq userId }) {
        with(SqlExpressionBuilder) {
            it.update(MemberTable.fixedPoints, MemberTable.fixedPoints + points)
        }
    }
}
