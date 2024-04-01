package scorcerer.server.resources

import org.openapitools.server.apis.LeagueApi
import org.openapitools.server.models.League
import org.openapitools.server.models.LeaguePost200Response
import org.openapitools.server.models.LeaguePostRequest

class League: LeagueApi() {
    override fun leagueLeagueIdGet(leagueId: String): League {
        TODO("Not yet implemented")
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