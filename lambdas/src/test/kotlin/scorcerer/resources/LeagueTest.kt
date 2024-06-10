package scorcerer.resources

import aws.sdk.kotlin.services.s3.S3Client
import io.kotlintest.inspectors.forOne
import io.kotlintest.shouldBe
import org.http4k.core.RequestContexts
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
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
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.resources.League
import scorcerer.utils.LeaderboardS3Service

class LeagueTest : DatabaseTest() {
    @BeforeEach
    fun generateUser() {
        givenUserExists("userId", "name")
    }

    private val mockS3Client: S3Client = mock(S3Client::class.java)
    private val mockLeaderboardService = LeaderboardS3Service(mockS3Client, "bucketName")

    @Test
    fun createLeague() {
        val league = League(RequestContexts(), mockLeaderboardService).createLeague(
            "userId",
            CreateLeagueRequest(
                "Test League",
            ),
        )
        league.leagueId shouldBe "test-league"
    }

    @Test
    fun createLeagueWithExtraWhitespace() {
        val league = League(RequestContexts(), mockLeaderboardService).createLeague(
            "userId",
            CreateLeagueRequest(
                " Test    League ",
            ),
        )
        league.leagueId shouldBe "test-league"
    }

    @Test
    fun getLeagueWhenNoUsersInLeague() {
        givenLeagueExists("test-league", "Test League")
        val league = League(RequestContexts(), mockLeaderboardService).getLeague(
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

        val league = League(RequestContexts(), mockLeaderboardService).getLeague(
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
            League(RequestContexts(), mockLeaderboardService).getLeague(
                "userId",
                "invalid-league",
            )
        }
    }

    @Test
    fun leaveLeague() {
        League(RequestContexts(), mockLeaderboardService).leaveLeague(
            "userId",
            "another-league",
        )
        // TODO: Assert on users in league once endpoint exists
    }

    @Test
    fun joinLeague() {
        givenLeagueExists("test-league", "Test League")
        givenUserExists("anotherUser", "test", fixedPoints = 0, livePoints = 0)
        League(RequestContexts(), mockLeaderboardService).joinLeague(
            "anotherUser",
            "test-league",
        )
        // TODO: Assert on users in league once endpoint exists
    }

    @Test
    fun joinLeagueTwice() {
        givenLeagueExists("test-league", "Test League")
        givenUserExists("anotherUser", "test", fixedPoints = 0, livePoints = 0)
        League(RequestContexts(), mockLeaderboardService).joinLeague(
            "anotherUser",
            "test-league",
        )

        League(RequestContexts(), mockLeaderboardService).joinLeague(
            "anotherUser",
            "test-league",
        )

        val memberships = transaction {
            LeagueMembershipTable
                .selectAll()
                .where { (LeagueMembershipTable.leagueId eq "test-league") and (LeagueMembershipTable.memberId eq "anotherUser") }
                .count()
        }
        memberships shouldBe 1
    }

    @Test
    fun createLeagueRaisesExceptionWhenLeagueExists() {
        givenLeagueExists("test-league", "Test League")
        assertThrows<Exception> {
            League(RequestContexts(), mockLeaderboardService).createLeague(
                "userId",
                CreateLeagueRequest("Test League"),
            )
        }
    }
}
