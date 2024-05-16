import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.openapitools.server.models.Match
import org.openapitools.server.models.Prediction
import scorcerer.utils.PointsCalculator
import java.time.OffsetDateTime

internal class PointsCalculatorTest {
    data class TestInput(val prediction: Prediction, val result: Match)

    @ParameterizedTest
    @CsvSource("1, 1, 1, 1, 5", "2, 1, 3, 1, 2", "2, 5, 1, 3, 2", "1, 1, 2, 2, 2", "1, 1, 0, 1, 0")
    fun testCalculatePoints(
        predictedHomeScore: Int,
        predictedAwayScore: Int,
        homeScore: Int,
        awayScore: Int,
        expectedPoints: Int,
    ) {
        val prediction = Prediction(predictedHomeScore, predictedAwayScore, "matchId", "predictionId", "userId")
        val result = Match("homeTeam", "", "awayTeam", "", "matchId", "", OffsetDateTime.now(), homeScore, awayScore)
        PointsCalculator.calculatePoints(prediction, result) shouldBe expectedPoints
    }

    @Test
    fun testCalulatePointsRaisesException() {
        assertThrows<IllegalArgumentException> {
            val prediction = Prediction(1, 1, "matchId", "predictionId", "userId")
            val result = Match("homeTeam", "", "awayTeam", "", "matchId", "", OffsetDateTime.now())
            PointsCalculator.calculatePoints(prediction, result)
        }
    }
}
