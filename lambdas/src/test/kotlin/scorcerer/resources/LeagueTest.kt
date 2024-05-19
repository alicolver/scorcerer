package scorcerer.resources

import aws.sdk.kotlin.services.s3.S3Client
import io.kotlintest.inspectors.forOne
import io.kotlintest.shouldBe
import org.http4k.core.RequestContexts
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.openapitools.server.models.CreateLeagueRequest
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

    private val mockS3Client: S3Client = mock(S3Client::class.java)

    @Test
    fun createLeague() {
        val league = League(RequestContexts(), mockS3Client, "leaderboardBucketName").createLeague(
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
        val league = League(RequestContexts(), mockS3Client, "leaderboardBucketName").getLeague(
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

        val league = League(RequestContexts(), mockS3Client, "leaderboardBucketName").getLeague(
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
            League(RequestContexts(), mockS3Client, "leaderboardBucketName").getLeague(
                "userId",
                "invalid-league",
            )
        }
    }

    @Test
    fun leaveLeague() {
        League(RequestContexts(), mockS3Client, "leaderboardBucketName").leaveLeague(
            "userId",
            "another-league",
        )
        // TODO: Assert on users in league once endpoint exists
    }

    @Test
    fun joinLeague() {
        givenLeagueExists("test-league", "Test League")
        givenUserExists("anotherUser", "test", 0, 0)
        League(RequestContexts(), mockS3Client, "leaderboardBucketName").joinLeague(
            "anotherUser",
            "test-league",
        )
        // TODO: Assert on users in league once endpoint exists
    }

    @Test
    fun createLeagueRaisesExceptionWhenLeagueExists() {
        givenLeagueExists("test-league", "Test League")
        assertThrows<Exception> {
            League(RequestContexts(), mockS3Client, "leaderboardBucketName").createLeague(
                "userId",
                CreateLeagueRequest("Test League"),
            )
        }
    }
}
