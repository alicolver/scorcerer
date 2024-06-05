package scorcerer.resources

import io.kotlintest.inspectors.forOne
import io.kotlintest.shouldBe
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.mockk
import org.http4k.core.RequestContexts
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.openapitools.server.models.CompleteMatchRequest
import org.openapitools.server.models.CreateMatchRequest
import org.openapitools.server.models.Match
import org.openapitools.server.models.Prediction
import org.openapitools.server.models.SetMatchScoreRequest
import scorcerer.*
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.MatchState
import scorcerer.server.db.tables.MatchTable
import scorcerer.server.db.tables.MemberTable
import scorcerer.server.db.tables.PredictionTable
import scorcerer.server.resources.MatchResource
import scorcerer.server.resources.getMatchesOnNextNMatchDays
import scorcerer.utils.LeaderboardS3Service
import scorcerer.utils.MatchResult
import java.time.OffsetDateTime

class MatchTest : DatabaseTest() {
    @BeforeEach
    fun generateTeams() {
        givenTeamExists("England")
        givenTeamExists("France")
        givenTeamExists("Spain")
        givenTeamExists("Scotland")
    }

    private val mockLeaderboardService = mockk<LeaderboardS3Service>(relaxed = true)

    @Test
    fun createMatch() {
        val match = MatchResource(RequestContexts(), mockLeaderboardService).createMatch(
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

        givenUserExists("test", "Test")
        givenPredictionExists("1", "test", 3, 4)

        val unfilteredMatches = MatchResource(RequestContexts(), mockLeaderboardService).listMatches("test", null)
        unfilteredMatches.size shouldBe 2

        val prediction = unfilteredMatches.first().prediction
        prediction shouldBe Prediction(3, 4, "1", "1", "test", null)

        MatchResource(RequestContexts(), mockLeaderboardService).listMatches(
            "",
            MatchState.UPCOMING.toString(),
        ).size shouldBe 2

        // There should be no matches which are LIVE or COMPLETED
        MatchResource(RequestContexts(), mockLeaderboardService).listMatches(
            "",
            MatchState.COMPLETED.toString(),
        ).size shouldBe 0
        MatchResource(RequestContexts(), mockLeaderboardService).listMatches(
            "",
            MatchState.LIVE.toString(),
        ).size shouldBe 0
    }

    @Test
    fun getMatchPredictionsWithNoLeagueFilter() {
        val userId = "userId"
        givenUserExists(userId, "name")
        val anotherUserId = "anotherUser"
        givenUserExists(anotherUserId, "name")

        val matchId = givenMatchExists("3", "4", matchState = MatchState.LIVE)
        val predictionId = givenPredictionExists(matchId, userId, 1, 1)
        val anotherPredictionId = givenPredictionExists(matchId, anotherUserId, 1, 1)

        val leagueId = "test-league"
        givenLeagueExists(leagueId, "Test League")
        givenUserInLeague(userId, leagueId)

        val anotherLeagueId = "another-test-league"
        givenLeagueExists(anotherLeagueId, "Test League")
        givenUserInLeague(anotherUserId, anotherLeagueId)

        val predictions =
            MatchResource(RequestContexts(), mockLeaderboardService).getMatchPredictions("", "1", null)
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
        givenUserExists(userId, "name", fixedPoints = 0, livePoints = 0)
        val anotherUserId = "anotherUser"
        givenUserExists(anotherUserId, "name", fixedPoints = 0, livePoints = 0)
        val matchId = givenMatchExists("3", "4", matchState = MatchState.LIVE)

        val predictionId = givenPredictionExists(matchId, userId, 1, 1)
        val leagueId = "test-league"
        givenLeagueExists(leagueId, "Test League")
        givenUserInLeague(userId, leagueId)

        givenPredictionExists(matchId, anotherUserId, 1, 1)
        val anotherLeagueId = "another-test-league"
        givenLeagueExists(anotherLeagueId, "Test League")
        givenUserInLeague(anotherUserId, anotherLeagueId)

        val matchPredictions =
            MatchResource(RequestContexts(), mockLeaderboardService).getMatchPredictions(
                "",
                "1",
                leagueId,
            )
        matchPredictions.size shouldBe 1
        matchPredictions[0].predictionId shouldBe predictionId
    }

    @Test
    fun getMatchPredictionsWhenNoMatchRaisesError() {
        assertThrows<ApiResponseError> {
            MatchResource(RequestContexts(), mockLeaderboardService).getMatchPredictions(
                "userId",
                "1",
                null,
            )
        }
    }

    @Test
    fun getMatchPredictionsWhenMatchUpcomingRaisesError() {
        val matchId = givenMatchExists("3", "4")
        assertThrows<ApiResponseError> {
            MatchResource(RequestContexts(), mockLeaderboardService).getMatchPredictions(
                "userId",
                matchId,
                null,
            )
        }
    }

    @Test
    fun setMatchScoreWhenMatchDoesNotExistRaises() {
        assertThrows<ApiResponseError> {
            MatchResource(RequestContexts(), mockLeaderboardService).setMatchScore(
                "",
                "1",
                SetMatchScoreRequest(1, 2),
            )
        }
    }

    @Test
    fun setMatchScoreWhenMatchExistsUpdatesScore() {
        val matchId = givenMatchExists("1", "2")
        MatchResource(RequestContexts(), mockLeaderboardService).setMatchScore(
            "",
            matchId,
            SetMatchScoreRequest(1, 2),
        )
        val match = transaction {
            MatchTable.selectAll().where { MatchTable.id eq matchId.toInt() }.map { row ->
                MatchResult(row[MatchTable.homeScore] ?: 0, row[MatchTable.awayScore] ?: 0)
            }
        }[0]
        match.awayScore shouldBe 2
        match.homeScore shouldBe 1
    }

    @Test
    fun setMatchScoreWhenPredictionExistsUpdatesPoints() {
        val matchId = givenMatchExists("1", "2", matchDay = 5)
        givenUserExists("userId", "name")
        val predictionId = givenPredictionExists(matchId, "userId", 1, 1)
        getPredictionPoints(predictionId) shouldBe null

        MatchResource(RequestContexts(), mockLeaderboardService).setMatchScore(
            "",
            matchId,
            SetMatchScoreRequest(0, 0),
        )
        getPredictionPoints(predictionId) shouldBe 2
        getLivePoints("userId") shouldBe 2

        MatchResource(RequestContexts(), mockLeaderboardService).setMatchScore(
            "",
            matchId,
            SetMatchScoreRequest(1, 1),
        )
        getPredictionPoints(predictionId) shouldBe 5
        getLivePoints("userId") shouldBe 5
        coVerifySequence {
            mockLeaderboardService.updateGlobalLeaderboard(5)
            mockLeaderboardService.updateGlobalLeaderboard(5)
        }
    }

    @Test
    fun setMatchScoreWhenOtherLiveGame() {
        val matchResource = MatchResource(RequestContexts(), mockLeaderboardService)
        val matchId = givenMatchExists("1", "2")
        val anotherMatchId = givenMatchExists("1", "2")
        givenUserExists("userId", "name")
        givenUserExists("userNoPredictions", "name")
        givenPredictionExists(matchId, "userId", 1, 1)
        givenPredictionExists(anotherMatchId, "userId", 1, 0)

        matchResource.setMatchScore("", matchId, SetMatchScoreRequest(0, 0))
        getLivePoints("userId") shouldBe 2
        getLivePoints("userNoPredictions") shouldBe 0

        matchResource.setMatchScore("", anotherMatchId, SetMatchScoreRequest(0, 0))
        getLivePoints("userId") shouldBe 2
        getLivePoints("userNoPredictions") shouldBe 0

        matchResource.setMatchScore("", anotherMatchId, SetMatchScoreRequest(1, 0))
        getLivePoints("userId") shouldBe 7
        getLivePoints("userNoPredictions") shouldBe 0
        coVerify { mockLeaderboardService.updateGlobalLeaderboard(1) }
    }

    @Test
    fun completeMatch() {
        val matchResource = MatchResource(RequestContexts(), mockLeaderboardService)
        val matchId = givenMatchExists("1", "2")
        givenUserExists("userId", "name")
        givenUserExists("anotherUser", "name", fixedPoints = 1)
        givenPredictionExists(matchId, "userId", 2, 1)
        givenPredictionExists(matchId, "anotherUser", 1, 0)

        matchResource.completeMatch("", matchId, CompleteMatchRequest(2, 1))
        getLivePoints("userId") shouldBe 0
        getLivePoints("anotherUser") shouldBe 0
        getFixedPoints("userId") shouldBe 5
        getFixedPoints("anotherUser") shouldBe 3
        transaction {
            MatchTable.select(MatchTable.state).where { MatchTable.id eq matchId.toInt() }
                .map { row -> row[MatchTable.state] }[0]
        } shouldBe MatchState.COMPLETED
        coVerify { mockLeaderboardService.updateGlobalLeaderboard(1) }
    }

    @Test
    fun completeMatchGivenLiveMatch() {
        val matchResource = MatchResource(RequestContexts(), mockLeaderboardService)
        val matchId = givenMatchExists("1", "2")
        val anotherMatchId = givenMatchExists("1", "2")
        matchResource.setMatchScore("", anotherMatchId, SetMatchScoreRequest(0, 0))
        givenUserExists("userId", "name")
        givenUserExists("anotherUser", "name", fixedPoints = 1)
        givenPredictionExists(matchId, "userId", 2, 1)
        givenPredictionExists(matchId, "anotherUser", 1, 0)
        givenPredictionExists(anotherMatchId, "userId", 0, 0)
        givenPredictionExists(anotherMatchId, "anotherUser", 1, 1)

        matchResource.setMatchScore("", matchId, SetMatchScoreRequest(2, 1))
        matchResource.setMatchScore("", anotherMatchId, SetMatchScoreRequest(0, 0))
        getLivePoints("userId") shouldBe 10
        getLivePoints("anotherUser") shouldBe 4
        getFixedPoints("userId") shouldBe 0
        getFixedPoints("anotherUser") shouldBe 1

        matchResource.completeMatch("", matchId, CompleteMatchRequest(2, 1))
        getLivePoints("userId") shouldBe 5
        getLivePoints("anotherUser") shouldBe 2
        getFixedPoints("userId") shouldBe 5
        getFixedPoints("anotherUser") shouldBe 3
        transaction {
            MatchTable.select(MatchTable.state).where { MatchTable.id eq matchId.toInt() }
                .map { row -> row[MatchTable.state] }[0]
        } shouldBe MatchState.COMPLETED
        coVerify { mockLeaderboardService.updateGlobalLeaderboard(1) }
    }
}

class GetMatchesOnNextNMatchDaysTest {
    @Test
    fun testWithMultipleMatchDays() {
        val matches = listOf(
            Match("Team A", "flagA", "Team B", "flagB", "1", "Stadium A", OffsetDateTime.now(), 1),
            Match("Team C", "flagC", "Team D", "flagD", "2", "Stadium B", OffsetDateTime.now(), 2),
            Match("Team E", "flagE", "Team F", "flagF", "3", "Stadium C", OffsetDateTime.now(), 2),
            Match("Team G", "flagG", "Team H", "flagH", "4", "Stadium D", OffsetDateTime.now(), 3),
        )

        val filteredMatches = getMatchesOnNextNMatchDays(matches)
        filteredMatches.size shouldBe 3
        filteredMatches.all { it.matchDay in listOf(1, 2) } shouldBe true
    }

    @Test
    fun testWithLessThanNMatchDays() {
        val matches = listOf(
            Match("Team A", "flagA", "Team B", "flagB", "1", "Stadium A", OffsetDateTime.now(), 1),
            Match("Team C", "flagC", "Team D", "flagD", "2", "Stadium B", OffsetDateTime.now(), 1),
        )

        val filteredMatches = getMatchesOnNextNMatchDays(matches)
        filteredMatches.size shouldBe 2
        filteredMatches.all { it.matchDay == 1 } shouldBe true
    }

    @Test
    fun testWithNoMatches() {
        val matches = emptyList<Match>()

        val filteredMatches = getMatchesOnNextNMatchDays(matches)
        filteredMatches.size shouldBe 0
    }
}

private fun getLivePoints(memberId: String): Int = transaction {
    MemberTable.select(MemberTable.livePoints).where { MemberTable.id eq memberId }
        .map { row -> row[MemberTable.livePoints] }[0]
}

private fun getFixedPoints(memberId: String): Int = transaction {
    MemberTable.select(MemberTable.fixedPoints).where { MemberTable.id eq memberId }
        .map { row -> row[MemberTable.fixedPoints] }[0]
}

private fun getPredictionPoints(predictionId: String): Int? = transaction {
    PredictionTable.select(PredictionTable.points).where { PredictionTable.id eq predictionId.toInt() }
        .map { row -> row[PredictionTable.points] }[0]
}
