import io.kotlintest.shouldBe
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import scorcerer.db.Prediction
import scorcerer.utils.PointsCalculator
import scorcerer.utils.Result

internal class PointsCalculatorTest {
    data class TestInput(val prediction: Prediction, val result: Result)

    @TestFactory
    fun testCalculatePoints() =
        listOf(
            TestInput(Prediction(1, 1, "test"), Result(1, 1)) to 5,
            TestInput(Prediction(2, 1, matchId = "test"), Result(3, 1)) to 2,
            TestInput(Prediction(2, 5, matchId = "test"), Result(1, 3)) to 2,
            TestInput(Prediction(1, 1, matchId = "test"), Result(2, 2)) to 2,
            TestInput(Prediction(1, 1, matchId = "test"), Result(0, 1)) to 0,
        ).map { (input, expected) ->
            DynamicTest.dynamicTest("test") {
                PointsCalculator.calculatePoints(input.prediction, input.result) shouldBe expected
            }
        }
}
