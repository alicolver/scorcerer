package scorcerer.resources

import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.openapitools.server.models.CreateLeagueRequest
import scorcerer.DatabaseTest
import scorcerer.givenLeagueExists
import scorcerer.givenUserExists
import scorcerer.server.resources.League

class LeagueTest : DatabaseTest() {
    @BeforeEach
    fun generateUser() {
        givenUserExists("testId", "name")
    }

    @Test
    fun createLeague() {
        val league = League().createLeague(
            "testId",
            CreateLeagueRequest(
                "Test League",
            ),
        )
        league.leagueId shouldBe "test-league"
    }

    @Test
    fun getLeague() {
        givenLeagueExists("test-league", "Test League")
        val league = League().getLeague(
            "testId",
            "test-league",
        )
        league.name shouldBe "Test League"
        league.leagueId shouldBe "test-league"
    }

    @Test
    fun leaveLeague() {
        League().leaveLeague(
            "testId",
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
                "testId",
                CreateLeagueRequest("Test League"),
            )
        }
    }
}
