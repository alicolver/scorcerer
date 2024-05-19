package scorcerer.resources

import aws.sdk.kotlin.services.s3.S3Client
import io.kotest.extensions.system.withEnvironment
import io.kotlintest.inspectors.forOne
import io.kotlintest.shouldBe
import org.http4k.core.RequestContexts
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
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

    val mockS3Client = mock(S3Client::class.java)

    @Test
    fun createLeague() {
        val league = League(RequestContexts(), mockS3Client).createLeague(
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
        val league = League(RequestContexts(), mockS3Client).getLeague(
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

        val league = League(RequestContexts(), mockS3Client).getLeague(
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
            League(RequestContexts(), mockS3Client).getLeague(
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

        withEnvironment(
            mapOf(
                "DB_USER" to "test",
                "DB_PASSWORD" to "test",
                "DB_URL" to "test",
                "DB_NAME" to "test",
                "LEADERBOARD_BUCKET_NAME" to "leaderboard_bucket",
                "USER_POOL_CLIENT_ID" to "test",
                "USER_POOL_ID" to "test",
                "USER_CREATION_QUEUE_URL" to "test",
            ),
        ) {
            val leagueLeaderboard = League(RequestContexts(), mockS3Client).getLeagueLeaderboard("", "test-league")
            leagueLeaderboard.size shouldBe 5

            leagueLeaderboard shouldBe listOf(
                LeaderboardInner(1, User("name5", "user5", 10, 5)),
                LeaderboardInner(2, User("name3", "user3", 5, 7)),
                LeaderboardInner(3, User("name1", "user1", 5, 5)),
                LeaderboardInner(3, User("name4", "user4", 5, 5)),
                LeaderboardInner(5, User("name2", "user2", 3, 4)),
            )
        }
    }

    @Test
    fun leaveLeague() {
        League(RequestContexts(), mockS3Client).leaveLeague(
            "userId",
            "another-league",
        )
        // TODO: Assert on users in league once endpoint exists
    }

    @Test
    fun joinLeague() {
        givenLeagueExists("test-league", "Test League")
        givenUserExists("anotherUser", "test", 0, 0)
        League(RequestContexts(), mockS3Client).joinLeague(
            "anotherUser",
            "test-league",
        )
        // TODO: Assert on users in league once endpoint exists
    }

    @Test
    fun createLeagueRaisesExceptionWhenLeagueExists() {
        givenLeagueExists("test-league", "Test League")
        assertThrows<Exception> {
            League(RequestContexts(), mockS3Client).createLeague(
                "userId",
                CreateLeagueRequest("Test League"),
            )
        }
    }
}
