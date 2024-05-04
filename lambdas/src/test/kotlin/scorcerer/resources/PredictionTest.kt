package scorcerer.resources

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.openapitools.server.models.CreatePredictionRequest
import scorcerer.*
import scorcerer.server.resources.Prediction

class PredictionTest : DatabaseTest() {
    @Test
    fun createPrediction() {
        givenUserExists("userId", "name")
        val homeTeamId = givenTeamExists("England")
        val awayTeamId = givenTeamExists("Scotland")
        val matchId = givenMatchExists(homeTeamId, awayTeamId)
        val prediction = Prediction().createPrediction(
            requesterUserId = "userId",
            CreatePredictionRequest(
                1,
                2,
                matchId,

            ),
        )

        prediction.predictionId shouldBe "1"
    }

    @Test
    fun createPredictionGivenPredictionExistsRaises() {
        givenUserExists("userId", "name")
        val homeTeamId = givenTeamExists("England")
        val awayTeamId = givenTeamExists("Scotland")
        val matchId = givenMatchExists(homeTeamId, awayTeamId)
        givenPredictionExists(matchId, "userId", 1, 1)
        assertThrows<Exception> {
            Prediction().createPrediction(
                requesterUserId = "userId",
                CreatePredictionRequest(
                    1,
                    2,
                    matchId,

                ),
            )
        }
    }
}
