import io.kotlintest.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import scorcerer.db.Prediction
import scorcerer.utils.PointsCalculator
import scorcerer.utils.Result

internal class PointsCalculatorTest {
    data class TestInput(val prediction: Prediction, val result: Result)

    @ParameterizedTest
    @CsvSource("1, 1, 1, 1, 5", "2, 1, 3, 1, 2", "2, 5, 1, 3, 2", "1, 1, 2, 2, 2", "1, 1, 0, 1, 0")
    fun testCalculatePoints(
        predictedHomeScore: Int,
        predictedAwayScore: Int,
        homeScore: Int,
        awayScore: Int,
        expectedPoints: Int,
    ) {
        val prediction = Prediction(predictedHomeScore, predictedAwayScore, "test")
        val result = Result(homeScore, awayScore)
        PointsCalculator.calculatePoints(prediction, result) shouldBe expectedPoints
    }
}
