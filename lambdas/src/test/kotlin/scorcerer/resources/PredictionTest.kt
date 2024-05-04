package scorcerer.resources

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.openapitools.server.models.CreatePredictionRequest
import scorcerer.DatabaseTest
import scorcerer.givenMatchExists
import scorcerer.givenTeamExists
import scorcerer.givenUserExists
import scorcerer.server.resources.Prediction

class PredictionTest : DatabaseTest() {
    @Test
    fun createPrediction() {
        givenUserExists("userId", "name")
        val homeTeamId = givenTeamExists("England")
        val awayTeamId = givenTeamExists("Scotland")
        givenMatchExists(homeTeamId, awayTeamId)
        val prediction = Prediction().createPrediction(
            requesterUserId = "userId",
            CreatePredictionRequest(
                1,
                2,
                "1",

            ),
        )

        prediction.predictionId shouldBe "1"
    }
}
