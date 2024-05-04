package scorcerer.resources

import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openapitools.server.models.CreateMatchRequest
import org.openapitools.server.models.CreatePredictionRequest
import org.openapitools.server.models.CreateTeamRequest
import org.openapitools.server.models.SignupRequest
import scorcerer.DatabaseTest
import scorcerer.server.resources.MatchResource
import scorcerer.server.resources.Prediction
import scorcerer.server.resources.Team
import scorcerer.server.resources.User
import java.time.OffsetDateTime

class PredictionTest : DatabaseTest() {

    @BeforeEach
    fun generateUser() {
        User().signup(SignupRequest("email", "password", "name"))
    }

    @BeforeEach
    fun generateMatch() {
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
    }

    @Test
    fun createPrediction() {
        val prediction = Prediction().createPrediction(
            requesterUserId = "id-from-cognito",
            CreatePredictionRequest(
                1,
                2,
                "1",

            ),
        )

        prediction.predictionId shouldBe "1"
    }
}
