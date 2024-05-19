package scorcerer.utils

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.openapitools.server.models.LeaderboardInner
import org.openapitools.server.models.User

class LeaderboardTest {
    @Test
    fun testFilterLeaderboardToLeague() {
        val globalLeagueLeaderboard = listOf(
            LeaderboardInner(1, User("name5", "user5", 10, 5)),
            LeaderboardInner(2, User("name3", "user3", 5, 7)),
            LeaderboardInner(3, User("name1", "user1", 5, 5)),
            LeaderboardInner(3, User("name4", "user4", 5, 5)),
            LeaderboardInner(5, User("name2", "user2", 3, 4)),
        )

        val leagueUserIds = listOf("user1", "user2", "user4", "user5")

        val filteredLeaderboard = filterLeaderboardToLeague(globalLeagueLeaderboard, leagueUserIds)
        filteredLeaderboard shouldBe listOf(
            LeaderboardInner(1, User("name5", "user5", 10, 5)),
            LeaderboardInner(2, User("name1", "user1", 5, 5)),
            LeaderboardInner(2, User("name4", "user4", 5, 5)),
            LeaderboardInner(4, User("name2", "user2", 3, 4)),
        )
    }
}
