package scorcerer.server.resources

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.MatchApi
import org.openapitools.server.models.CreateMatch200Response
import org.openapitools.server.models.CreateMatchRequest
import org.openapitools.server.models.Match
import org.openapitools.server.models.Prediction
import org.openapitools.server.models.SetMatchScoreRequest
import org.postgresql.util.PSQLException
import scorcerer.server.db.tables.MatchState
import scorcerer.server.db.tables.MatchTable

class MatchResource : MatchApi() {
    override fun getMatchPredictions(requesterUserId: String, matchId: String, leagueId: String?): List<Prediction> {
        TODO("Not yet implemented")
    }

    override fun listMatches(requesterUserId: String, filterType: String?): List<Match> {
        return listOf(
            Match("England", "France", "12345"),
            Match("Scotland", "Germany", "12346"),
        )
    }

    override fun setMatchScore(requesterUserId: String, matchId: String, setMatchScoreRequest: SetMatchScoreRequest) {
        TODO("Not yet implemented")
    }

    override fun createMatch(requesterUserId: String, createMatchRequest: CreateMatchRequest): CreateMatch200Response {
        val id = try {
            transaction {
                MatchTable.insert {
                    it[this.homeTeamId] = createMatchRequest.homeTeamId
                    it[this.awayTeamId] = createMatchRequest.awayTeamId
                    it[this.datetime] = createMatchRequest.datetime
                    it[this.state] = MatchState.UPCOMING
                    it[this.venue] = createMatchRequest.venue
                    it[this.matchDay] = createMatchRequest.matchDay
                } get MatchTable.id
            }
        } catch (e: PSQLException) {
            throw e
        }
        return CreateMatch200Response(id.toString())
    }
}
