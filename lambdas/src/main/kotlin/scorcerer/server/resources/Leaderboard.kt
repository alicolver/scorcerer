package scorcerer.server.resources

import org.openapitools.server.apis.LeaderboardApi
import org.openapitools.server.models.LeaderboardInner

class Leaderboard : LeaderboardApi() {
    override fun getLeaderboard(requesterUserId: String, leagueId: String?): List<LeaderboardInner> {
        TODO("Not yet implemented")
    }
}
