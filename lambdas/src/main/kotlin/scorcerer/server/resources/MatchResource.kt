package scorcerer.server.resources

import org.http4k.core.Response
import org.http4k.core.Status
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.MatchApi
import org.openapitools.server.models.*
import org.openapitools.server.models.Prediction
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.*
import scorcerer.utils.PointsCalculator
import scorcerer.utils.PointsCalculator.calculatePoints
import java.time.OffsetDateTime

class MatchResource : MatchApi() {
    override fun getMatchPredictions(requesterUserId: String, matchId: String, leagueId: String?): List<Prediction> {
        return transaction {
            if (leagueId.isNullOrBlank()) {
                PredictionTable.selectAll().where { (PredictionTable.matchId eq matchId.toInt()) }
            } else {
                PredictionTable.selectAll().where {
                    (PredictionTable.matchId eq matchId.toInt()).and(
                        PredictionTable.memberId inList LeagueMembershipTable.selectAll()
                            .where { LeagueMembershipTable.leagueId eq leagueId }
                            .map { row -> row[LeagueMembershipTable.memberId] },
                    )
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

    override fun listMatches(requesterUserId: String, filterType: String?): List<Match> = transaction {
        val awayTeamTable = TeamTable.alias("awayTeam")
        val homeTeamTable = TeamTable.alias("homeTeam")
        val matchTeamTable = MatchTable.join(awayTeamTable, JoinType.INNER, MatchTable.awayTeamId, awayTeamTable[TeamTable.id])
            .join(homeTeamTable, JoinType.INNER, MatchTable.homeTeamId, homeTeamTable[TeamTable.id])
        if (filterType.isNullOrBlank()) {
            matchTeamTable.selectAll()
        } else {
            matchTeamTable.selectAll().where { MatchTable.state eq MatchState.valueOf(filterType.uppercase()) }
        }.map { row ->
            Match(
                row[homeTeamTable[TeamTable.name]],
                row[homeTeamTable[TeamTable.flagUri]],
                row[awayTeamTable[TeamTable.name]],
                row[awayTeamTable[TeamTable.flagUri]],
                row[MatchTable.id].toString(),
                row[MatchTable.venue],
                row[MatchTable.datetime],
            )
        }
    }

    override fun setMatchScore(
        requesterUserId: String,
        matchId: String,
        setMatchScoreRequest: SetMatchScoreRequest,
    ) {
        val awayTeamTable = TeamTable.alias("awayTeam")
        val homeTeamTable = TeamTable.alias("homeTeam")
        val match = transaction {
            MatchTable.join(awayTeamTable, JoinType.INNER, MatchTable.awayTeamId, awayTeamTable[TeamTable.id])
                .join(homeTeamTable, JoinType.INNER, MatchTable.homeTeamId, homeTeamTable[TeamTable.id]).selectAll()
                .where { MatchTable.id eq matchId.toInt() }.firstOrNull()?.let { row ->
                    Match(
                        row[homeTeamTable[TeamTable.name]],
                        row[homeTeamTable[TeamTable.flagUri]],
                        row[awayTeamTable[TeamTable.name]],
                        row[awayTeamTable[TeamTable.flagUri]],
                        row[MatchTable.id].toString(),
                        row[MatchTable.venue],
                        row[MatchTable.datetime],
                        setMatchScoreRequest.homeScore,
                        setMatchScoreRequest.awayScore,
                    )
                } ?: throw ApiResponseError(Response(Status.BAD_REQUEST).body("Match does not exist"))
        }
        transaction {
            MatchTable.update({ MatchTable.id eq matchId.toInt() }) {
                it[homeScore] = setMatchScoreRequest.homeScore
                it[awayScore] = setMatchScoreRequest.awayScore
            }
        }
        transaction {
            PredictionTable.selectAll().where { PredictionTable.matchId eq matchId.toInt() }.forEach { row ->
                val prediction = Prediction(
                    row[PredictionTable.homeScore],
                    row[PredictionTable.awayScore],
                    row[PredictionTable.matchId].toString(),
                    row[PredictionTable.id].toString(),
                    row[PredictionTable.memberId],
                )
                val calculatedPoints = PointsCalculator.calculatePoints(prediction, match)

                PredictionTable.update({ PredictionTable.id eq row[PredictionTable.id] }) {
                    it[points] = calculatedPoints
                }
            }
        }
        // recalculate livePoints on each user
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
                it[this.state] = MatchState.UPCOMING
                it[this.venue] = createMatchRequest.venue
                it[this.matchDay] = createMatchRequest.matchDay
            } get MatchTable.id
        }
        return CreateMatch200Response(id.toString())
    }

    override fun completeMatch(
        requesterUserId: String,
        matchId: String,
        completeMatchRequest: CompleteMatchRequest,
    ) {
        transaction {
            MatchTable.update({ MatchTable.id eq matchId.toInt() }) {
                it[state] = MatchState.COMPLETED
                it[homeScore] = completeMatchRequest.homeScore
                it[awayScore] = completeMatchRequest.awayScore
            }

            val predictions =
                PredictionTable.selectAll().where { PredictionTable.matchId eq matchId.toInt() }.map { row ->
                    Prediction(
                        row[PredictionTable.homeScore],
                        row[PredictionTable.awayScore],
                        row[PredictionTable.matchId].toString(),
                        row[PredictionTable.id].toString(),
                        row[PredictionTable.memberId],
                    )
                }

            predictions.forEach { prediction ->
                val points = calculatePoints(
                    prediction,
//                  // TODO: have a model where we don't need all this junk
                    Match(
                        "",
                        "",
                        "",
                        "",
                        matchId,
                        "",
                        OffsetDateTime.now(),
                        completeMatchRequest.homeScore,
                        completeMatchRequest.awayScore,
                    ),
                )
                PredictionTable.update({ PredictionTable.id eq prediction.predictionId.toInt() }) {
                    it[PredictionTable.points] = points
                }
                MemberTable.update({ MemberTable.id eq prediction.userId }) {
                    with(SqlExpressionBuilder) {
                        it.update(MemberTable.fixedPoints, MemberTable.fixedPoints + points)
                    }
                }
            }
        }
    }
}
