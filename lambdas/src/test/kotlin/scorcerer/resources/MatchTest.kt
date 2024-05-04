package scorcerer.resources

import io.kotlintest.inspectors.forOne
import io.kotlintest.matchers.match
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.openapitools.server.models.CreateMatchRequest
import org.openapitools.server.models.SetMatchScoreRequest
import scorcerer.DatabaseTest
import scorcerer.givenLeagueExists
import scorcerer.givenMatchExists
import scorcerer.givenPredictionExists
import scorcerer.givenTeamExists
import scorcerer.givenUserExists
import scorcerer.givenUserInLeague
import scorcerer.server.db.tables.MatchState
import scorcerer.server.resources.MatchResource
import java.time.OffsetDateTime

class MatchTest : DatabaseTest() {
    @BeforeEach
    fun generateTeams() {
        givenTeamExists("England")
        givenTeamExists("France")
        givenTeamExists("Spain")
        givenTeamExists("Scotland")
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
        givenMatchExists("1", "2")
        givenMatchExists("3", "4")

        MatchResource().listMatches("", null).size shouldBe 2
        MatchResource().listMatches("", MatchState.UPCOMING.toString()).size shouldBe 2

        // There should be no matches which are LIVE or COMPLETED
        MatchResource().listMatches("", MatchState.COMPLETED.toString()).size shouldBe 0
        MatchResource().listMatches("", MatchState.LIVE.toString()).size shouldBe 0
    }

    @Test
    fun getMatchPredictionsWithNoLeagueFilter() {
        val userId = "userId"
        givenUserExists(userId, "name")
        val anotherUserId = "anotherUser"
        givenUserExists(anotherUserId, "name")

        val matchId = givenMatchExists("3", "4")
        val predictionId = givenPredictionExists(matchId, userId, 1, 1)
        val anotherPredictionId = givenPredictionExists(matchId, anotherUserId, 1, 1)

        val leagueId = "test-league"
        givenLeagueExists(leagueId, "Test League")
        givenUserInLeague(userId, leagueId)

        val anotherLeagueId = "another-test-league"
        givenLeagueExists(anotherLeagueId, "Test League")
        givenUserInLeague(anotherUserId, anotherLeagueId)

        val predictions = MatchResource().getMatchPredictions("", "1", null)
        predictions.size shouldBe 2
        predictions.forOne { prediction ->
            prediction.predictionId shouldBe predictionId
        }
        predictions.forOne { prediction ->
            prediction.predictionId shouldBe anotherPredictionId
        }
    }

    @Test
    fun getMatchPredictionsWithLeagueFilter() {
        val userId = "userId"
        givenUserExists(userId, "name", 0, 0)
        val anotherUserId = "anotherUser"
        givenUserExists(anotherUserId, "name", 0, 0)
        val matchId = givenMatchExists("3", "4")

        val predictionId = givenPredictionExists(matchId, userId, 1, 1)
        val leagueId = "test-league"
        givenLeagueExists(leagueId, "Test League")
        givenUserInLeague(userId, leagueId)

        givenPredictionExists(matchId, anotherUserId, 1, 1)
        val anotherLeagueId = "another-test-league"
        givenLeagueExists(anotherLeagueId, "Test League")
        givenUserInLeague(anotherUserId, anotherLeagueId)

        val matchPredictions = MatchResource().getMatchPredictions("", "1", leagueId)
        matchPredictions.size shouldBe 1
        matchPredictions[0].predictionId shouldBe predictionId
    }

    @Test
    fun setMatchScore() {
        assertThrows<NotImplementedError> {
            MatchResource().setMatchScore("", "", SetMatchScoreRequest(1, 2))
        }
    }
}
