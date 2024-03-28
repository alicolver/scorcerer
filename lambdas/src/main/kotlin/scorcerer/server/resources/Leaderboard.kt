package scorcerer.server.resources

import org.openapitools.server.apis.DefaultApi
import org.openapitools.server.models.LeaderboardInner

class Leaderboard: DefaultApi() {


    override fun leaderboardGet(leagueId: String?): List<LeaderboardInner> {
        TODO("Not yet implemented")
    }
}