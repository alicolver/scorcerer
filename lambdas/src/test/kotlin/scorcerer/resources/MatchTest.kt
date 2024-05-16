package scorcerer.resources

import io.kotlintest.inspectors.forOne
import io.kotlintest.shouldBe
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.openapitools.server.models.CreateMatchRequest
import org.openapitools.server.models.Match
import org.openapitools.server.models.SetMatchScoreRequest
import scorcerer.*
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.MatchState
import scorcerer.server.db.tables.MatchTable
import scorcerer.server.db.tables.PredictionTable
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

        val allMatches = MatchResource().listMatches("", null)

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
    fun setMatchScoreWhenMatchDoesNotExistRaises() {
        assertThrows<ApiResponseError> {
            MatchResource().setMatchScore("", "1", SetMatchScoreRequest(1, 2))
        }
    }

    @Test
    fun setMatchScoreWhenMatchExistsUpdatesScore() {
        val matchId = givenMatchExists("1", "2")
        MatchResource().setMatchScore("", matchId, SetMatchScoreRequest(1, 2))
        val match = transaction {
            MatchTable.selectAll().where { MatchTable.id eq matchId.toInt() }.map { row ->
                Match(
                    row[MatchTable.homeTeamId].toString(),
                    "",
                    row[MatchTable.awayTeamId].toString(),
                    "",
                    row[MatchTable.id].toString(),
                    "",
                    OffsetDateTime.now(),
                    row[MatchTable.homeScore],
                    row[MatchTable.awayScore],
                )
            }
        }[0]
        match.awayScore shouldBe 2
        match.homeScore shouldBe 1
    }

    @Test
    fun setMatchScoreWhenPredictionExistsUpdatesPoints() {
        val matchId = givenMatchExists("1", "2")
        givenUserExists("userId", "name")
        val predictionId = givenPredictionExists(matchId, "userId", 1, 1)
        val predictionPoints = transaction {
            PredictionTable.selectAll().where { PredictionTable.id eq predictionId.toInt() }.map { row ->
                row[PredictionTable.points]
            }
        }[0]
        predictionPoints shouldBe null

        MatchResource().setMatchScore("", matchId, SetMatchScoreRequest(0, 0))
        val predictionPointsUpdated = transaction {
            PredictionTable.selectAll().where { PredictionTable.id eq predictionId.toInt() }.map { row ->
                row[PredictionTable.points]
            }
        }[0]
        predictionPointsUpdated shouldBe 2

        MatchResource().setMatchScore("", matchId, SetMatchScoreRequest(1, 1))
        val predictionPointsUpdatedAgain = transaction {
            PredictionTable.selectAll().where { PredictionTable.id eq predictionId.toInt() }.map { row ->
                row[PredictionTable.points]
            }
        }[0]
        predictionPointsUpdatedAgain shouldBe 5
    }
}
