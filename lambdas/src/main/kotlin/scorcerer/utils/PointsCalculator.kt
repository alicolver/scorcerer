package scorcerer.utils

import scorcerer.db.Prediction

data class Result(val homeScore: Int, val awayScore: Int)

object PointsCalculator {
    fun calculatePoints(
        prediction: Prediction,
        result: Result,
    ) = when {
        prediction.awayScore == result.awayScore && prediction.homeScore == result.homeScore -> 5
        prediction.homeScore < prediction.awayScore && result.homeScore < result.awayScore -> 2
        prediction.homeScore > prediction.awayScore && result.homeScore > result.awayScore -> 2
        prediction.homeScore == prediction.awayScore && result.homeScore == result.awayScore -> 2
        else -> 0
    }
}
