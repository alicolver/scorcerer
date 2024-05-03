package scorcerer.resources

import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openapitools.server.models.CreateMatchRequest
import org.openapitools.server.models.CreateTeamRequest
import scorcerer.DatabaseTest
import scorcerer.server.db.tables.MatchState
import scorcerer.server.resources.MatchResource
import scorcerer.server.resources.Team
import java.time.OffsetDateTime

class MatchTest : DatabaseTest() {
    @BeforeEach
    fun generateTeams() {
        Team().createTeam("", CreateTeamRequest("England", ""))
        Team().createTeam("", CreateTeamRequest("France", ""))
        Team().createTeam("", CreateTeamRequest("Spain", ""))
        Team().createTeam("", CreateTeamRequest("Scotland", ""))
    }

    @Test
    fun createMatch() {
        val match = MatchResource().createMatch(
            "",
            CreateMatchRequest(
                "1",
                "2",
                OffsetDateTime.now(),
                "Allianz",
                1,
            ),
        )

        match.matchId shouldBe "1"
    }

    @Test
    fun listMatches() {
        MatchResource().createMatch(
            "",
            CreateMatchRequest(
                "1",
                "2",
                OffsetDateTime.now(),
                "Allianz",
                1,
            ),
        )

        MatchResource().createMatch(
            "",
            CreateMatchRequest(
                "3",
                "4",
                OffsetDateTime.now(),
                "Signal Iduna Park",
                2,
            ),
        )

        MatchResource().listMatches("", null).size shouldBe 2
        MatchResource().listMatches("", MatchState.UPCOMING.toString()).size shouldBe 2

        // There should be no matches which are LIVE or COMPLETED
        MatchResource().listMatches("", MatchState.COMPLETED.toString()).size shouldBe 0
        MatchResource().listMatches("", MatchState.LIVE.toString()).size shouldBe 0
    }
}
