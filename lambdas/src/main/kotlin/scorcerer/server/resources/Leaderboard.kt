package scorcerer.server.resources

import jakarta.ws.rs.Path
import org.openapitools.server.apis.DefaultApi
import org.openapitools.server.models.LeaderboardInner
import org.openapitools.server.models.User

@Path("/")
class Leaderboard: DefaultApi {
    override fun leaderboardGet(leagueId: String?): List<LeaderboardInner> {
        return listOf(
            LeaderboardInner(1, User("Test", "user-1"))
        )
    }
}