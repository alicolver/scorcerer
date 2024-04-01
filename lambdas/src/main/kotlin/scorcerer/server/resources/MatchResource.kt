package scorcerer.server.resources

import org.openapitools.server.apis.MatchApi
import org.openapitools.server.models.Match
import org.openapitools.server.models.MatchMatchIdScorePostRequest
import org.openapitools.server.models.Prediction

class MatchResource : MatchApi() {
    override fun matchListGet(filterType: String?): List<Match> {
        return listOf(
            Match("England", "France", "12345"),
        )
    }

    override fun matchMatchIdPredictionsGet(
        matchId: String,
        leagueId: String?,
    ): List<Prediction> {
        TODO("Not yet implemented")
    }

    override fun matchMatchIdScorePost(
        matchId: String,
        matchMatchIdScorePostRequest: MatchMatchIdScorePostRequest,
    ) {
        TODO("Not yet implemented")
    }
}
