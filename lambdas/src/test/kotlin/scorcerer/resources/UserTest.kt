package scorcerer.resources

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.openapitools.server.models.CreateMatchRequest
import org.openapitools.server.models.CreatePredictionRequest
import org.openapitools.server.models.CreateTeamRequest
import scorcerer.DatabaseTest
import scorcerer.givenUserExists
import scorcerer.server.resources.MatchResource
import scorcerer.server.resources.Prediction
import scorcerer.server.resources.Team
import scorcerer.server.resources.User
import java.time.OffsetDateTime

class UserTest : DatabaseTest() {
    @Test
    fun getUserPoints() {
        givenUserExists("userId", "name", 15, 5)
        val userPoints = User().getUserPoints("", "userId")
        userPoints.livePoints shouldBe 5
        userPoints.fixedPoints shouldBe 15
    }

    @Test
    fun getUserPredictions() {
        givenUserExists("userId", "name", 15, 5)
        Team().createTeam("", CreateTeamRequest("England", ""))
        Team().createTeam("", CreateTeamRequest("France", ""))

        MatchResource().createMatch(
            "",
            CreateMatchRequest(
                "1",
                "2",
                OffsetDateTime.now(),
                "Oliphant Gardens",
                1,
            ),
        )
        Prediction().createPrediction("userId", CreatePredictionRequest(1, 1, "1"))
        User().getUserPredictions("", "userId").size shouldBe 1
    }
}
