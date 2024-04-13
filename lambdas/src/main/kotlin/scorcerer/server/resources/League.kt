package scorcerer.server.resources

import org.openapitools.server.apis.LeagueApi
import org.openapitools.server.models.CreateLeague200Response
import org.openapitools.server.models.CreateLeagueRequest
import org.openapitools.server.models.League

class League : LeagueApi() {
    override fun createLeague(requesterUserId: String, createLeagueRequest: CreateLeagueRequest): CreateLeague200Response {
        TODO("Not yet implemented")
    }

    override fun getLeague(requesterUserId: String, leagueId: String): League {
        TODO("Not yet implemented")
    }

    override fun joinLeague(requesterUserId: String, leagueId: String) {
        TODO("Not yet implemented")
    }

    override fun leaveLeague(requesterUserId: String, leagueId: String) {
        TODO("Not yet implemented")
    }
}
