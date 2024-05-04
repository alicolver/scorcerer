package scorcerer.server.resources

import org.http4k.core.Response
import org.http4k.core.Status
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.openapitools.server.apis.MatchApi
import org.openapitools.server.models.*
import org.openapitools.server.models.Prediction
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.db.tables.MatchState
import scorcerer.server.db.tables.MatchTable
import scorcerer.server.db.tables.PredictionTable
import scorcerer.utils.PointsCalculator

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
                    row[PredictionTable.points],
                )
            }
        }
    }

    override fun listMatches(requesterUserId: String, filterType: String?): List<Match> = transaction {
        if (filterType.isNullOrBlank()) {
            MatchTable.selectAll()
        } else {
            MatchTable.selectAll().where { MatchTable.state eq MatchState.valueOf(filterType.uppercase()) }
        }.map { row ->
            Match(
                row[MatchTable.homeTeamId].toString(),
                row[MatchTable.awayTeamId].toString(),
                row[MatchTable.id].toString(),
            )
        }
    }

    override fun setMatchScore(
        requesterUserId: String,
        matchId: String,
        setMatchScoreRequest: SetMatchScoreRequest,
    ) {
        val match = transaction {
            MatchTable.selectAll().where { MatchTable.id eq matchId.toInt() }.firstOrNull()?.let { row ->
                Match(
                    row[MatchTable.homeTeamId].toString(),
                    row[MatchTable.awayTeamId].toString(),
                    row[MatchTable.id].toString(),
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
}
