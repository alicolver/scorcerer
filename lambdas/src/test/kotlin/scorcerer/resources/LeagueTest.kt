package scorcerer.resources

import io.kotlintest.inspectors.forOne
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.openapitools.server.models.CreateLeagueRequest
import org.openapitools.server.models.LeaderboardInner
import org.openapitools.server.models.User
import scorcerer.DatabaseTest
import scorcerer.givenLeagueExists
import scorcerer.givenUserExists
import scorcerer.givenUserInLeague
import scorcerer.server.ApiResponseError
import scorcerer.server.resources.League

class LeagueTest : DatabaseTest() {
    @BeforeEach
    fun generateUser() {
        givenUserExists("userId", "name")
    }

    @Test
    fun createLeague() {
        val league = League().createLeague(
            "userId",
            CreateLeagueRequest(
                "Test League",
            ),
        )
        league.leagueId shouldBe "test-league"
    }

    @Test
    fun getLeagueWhenNoUsersInLeague() {
        givenLeagueExists("test-league", "Test League")
        val league = League().getLeague(
            "userId",
            "test-league",
        )
        league.name shouldBe "Test League"
        league.leagueId shouldBe "test-league"
        league.users.size shouldBe 0
    }

    @Test
    fun getLeagueWhenUsersInLeague() {
        val leagueId = "test-league"
        givenLeagueExists(leagueId, "Test League")
        givenUserExists("anotherUserId", "Another User")

        givenUserInLeague("userId", leagueId)
        givenUserInLeague("anotherUserId", leagueId)

        val league = League().getLeague(
            "userId",
            "test-league",
        )
        league.users.size shouldBe 2
        league.users.forOne { user ->
            user.userId shouldBe "userId"
        }
        league.users.forOne { user ->
            user.userId shouldBe "anotherUserId"
        }
    }

    @Test
    fun getLeagueRaisesWhenLeagueDoesNotExist() {
        assertThrows<ApiResponseError> {
            League().getLeague(
                "userId",
                "invalid-league",
            )
        }
    }

    @Test
    fun getLeagueLeaderboard() {
        givenUserExists("user1", "name1", 5, 5)
        givenUserExists("user2", "name2", 3, 4)
        givenUserExists("user3", "name3", 5, 7)
        givenUserExists("user4", "name4", 5, 5)
        givenUserExists("user5", "name5", 10, 5)

        givenLeagueExists("test-league", "Test League")

        listOf("user1", "user2", "user3", "user4", "user5").map { userId ->
            givenUserInLeague(userId, "test-league")
        }

        val leagueLeaderboard = League().getLeagueLeaderboard("", "test-league")
        leagueLeaderboard.size shouldBe 5

        leagueLeaderboard shouldBe listOf(
            LeaderboardInner(1, User("name5", "user5", 10, 5)),
            LeaderboardInner(2, User("name3", "user3", 5, 7)),
            LeaderboardInner(3, User("name1", "user1", 5, 5)),
            LeaderboardInner(3, User("name4", "user4", 5, 5)),
            LeaderboardInner(5, User("name2", "user2", 3, 4)),
        )
    }

    @Test
    fun leaveLeague() {
        League().leaveLeague(
            "userId",
            "another-league",
        )
        // TODO: Assert on users in league once endpoint exists
    }

    @Test
    fun joinLeague() {
        givenLeagueExists("test-league", "Test League")
        givenUserExists("anotherUser", "test", 0, 0)
        League().joinLeague(
            "anotherUser",
            "test-league",
        )
        // TODO: Assert on users in league once endpoint exists
    }

    @Test
    fun createLeagueRaisesExceptionWhenLeagueExists() {
        givenLeagueExists("test-league", "Test League")
        assertThrows<Exception> {
            League().createLeague(
                "userId",
                CreateLeagueRequest("Test League"),
            )
        }
    }
}
