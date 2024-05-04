package scorcerer.utils

import org.openapitools.server.models.Match
import org.openapitools.server.models.Prediction

object PointsCalculator {
    fun calculatePoints(prediction: Prediction, match: Match): Int {
        if (match.awayScore == null || match.homeScore == null) {
            throw IllegalArgumentException("Result scores cannot be null")
        }
        return when {
            prediction.awayScore == match.awayScore && prediction.homeScore == match.homeScore -> 5
            prediction.homeScore < prediction.awayScore && match.homeScore < match.awayScore -> 2
            prediction.homeScore > prediction.awayScore && match.homeScore > match.awayScore -> 2
            prediction.homeScore == prediction.awayScore && match.homeScore == match.awayScore -> 2
            else -> 0
        }
    }
}
