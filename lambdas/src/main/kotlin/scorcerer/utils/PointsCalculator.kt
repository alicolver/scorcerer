package scorcerer.utils

import org.openapitools.server.models.Match
import org.openapitools.server.models.Prediction

data class MatchResult(
    val homeScore: Int,
    val awayScore: Int
)
object PointsCalculator {
    fun calculatePoints(prediction: Prediction, result: MatchResult): Int {
        return when {
            prediction.awayScore == result.awayScore && prediction.homeScore == result.homeScore -> 5
            prediction.homeScore < prediction.awayScore && result.homeScore < result.awayScore -> 2
            prediction.homeScore > prediction.awayScore && result.homeScore > result.awayScore -> 2
            prediction.homeScore == prediction.awayScore && result.homeScore == result.awayScore -> 2
            else -> 0
        }
    }
}
