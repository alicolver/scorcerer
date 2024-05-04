package scorcerer.resources

import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openapitools.server.models.CreateLeagueRequest
import org.openapitools.server.models.SignupRequest
import scorcerer.DatabaseTest
import scorcerer.server.resources.League
import scorcerer.server.resources.User

class LeagueTest : DatabaseTest() {
    @BeforeEach
    fun generateUser() {
        User().signup(SignupRequest("email", "password", "name"))
    }

    @Test
    fun createLeague() {
        val league = League().createLeague(
            "id-from-cognito",
            CreateLeagueRequest(
                "Test League",
            ),
        )
        league.leagueId shouldBe "test-league"
    }

    @Test
    fun getLeague() {
        League().createLeague(
            "id-from-cognito",
            CreateLeagueRequest(
                "Another League",
            ),
        )
        val league = League().getLeague(
            "id-from-cognito",
            "another-league",
        )
        league.name shouldBe "Another League"
        league.leagueId shouldBe "another-league"
    }

    @Test
    fun leaveLeague() {
        League().leaveLeague(
            "id-from-cognito",
            "another-league",
        )
    }
}
