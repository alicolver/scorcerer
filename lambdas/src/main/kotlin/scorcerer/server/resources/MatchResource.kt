package scorcerer.server.resources

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.MatchApi
import org.openapitools.server.models.CreateMatch200Response
import org.openapitools.server.models.CreateMatchRequest
import org.openapitools.server.models.Match
import org.openapitools.server.models.Prediction
import org.openapitools.server.models.SetMatchScoreRequest
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.db.tables.MatchState
import scorcerer.server.db.tables.MatchTable
import scorcerer.server.db.tables.PredictionTable

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
            }
                .map { row ->
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
        TODO("Not yet implemented")
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
