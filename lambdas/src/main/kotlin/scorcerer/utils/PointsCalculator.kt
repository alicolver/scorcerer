package scorcerer.utils

import scorcerer.db.Prediction

data class Result(val homeScore: Int, val awayScore: Int)

class PointsCalculator {
    fun calculatePoints(
        prediction: Prediction,
        result: Result,
    ): Int {
        // correct score 5 points
        if (
            prediction.awayScore == result.awayScore &&
            prediction.homeScore == result.homeScore
        ) {
            return 5
        }

        // correct result 2 points
        if (
            prediction.homeScore < prediction.awayScore &&
            result.homeScore < result.awayScore
        ) {
            return 2
        }
        if (
            prediction.homeScore > prediction.awayScore &&
            result.homeScore > result.awayScore
        ) {
            return 2
        }
        if (
            prediction.homeScore == prediction.awayScore &&
            result.homeScore == result.awayScore
        ) {
            return 2
        }

        return 0
    }
}
