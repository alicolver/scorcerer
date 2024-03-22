package scorcerer.server.resources

import jakarta.ws.rs.Path
import org.openapitools.server.apis.LeagueApi
import org.openapitools.server.models.League
import org.openapitools.server.models.LeaguePost200Response
import org.openapitools.server.models.LeaguePostRequest

@Path("/")
class League: LeagueApi {
    override fun leagueLeagueIdGet(leagueId: String): League {
        return League(leagueId, "league-name")
    }

    override fun leagueLeagueIdJoinPost(leagueId: String) {
        TODO("Not yet implemented")
    }

    override fun leagueLeagueIdLeavePost(leagueId: String) {
        TODO("Not yet implemented")
    }

    override fun leaguePost(leaguePostRequest: LeaguePostRequest): LeaguePost200Response {
        TODO("Not yet implemented")
    }
}