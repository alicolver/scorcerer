package scorcerer.resources

import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.openapitools.server.models.CreateLeagueRequest
import scorcerer.DatabaseTest
import scorcerer.givenUserExists
import scorcerer.server.resources.League

class LeagueTest : DatabaseTest() {
    @BeforeEach
    fun generateUser() {
        givenUserExists("testId", "name", 0, 0)
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
        League().createLeague(
            "testId",
            CreateLeagueRequest(
                "Another League",
            ),
        )
        val league = League().getLeague(
            "testId",
            "another-league",
        )
        league.name shouldBe "Another League"
        league.leagueId shouldBe "another-league"
    }

    @Test
    fun leaveLeague() {
        League().leaveLeague(
            "testId",
            "another-league",
        )
    }

    @Test
    fun joinLeague() {
        League().createLeague(
            "testId",
            CreateLeagueRequest(
                "Another Test League",
            ),
        )
        givenUserExists("anotherUser", "test", 0, 0)
        League().joinLeague(
            "anotherUser",
            "another-test-league",
        )
    }

    @Test
    fun createLeagueRaisesExceptionWhenLeagueExists() {
        League().createLeague(
            "testId",
            CreateLeagueRequest("Duplicate League"),
        )
        assertThrows<Exception> {
            League().createLeague(
                "testId",
                CreateLeagueRequest("Duplicate League"),
            )
        }
    }
}
