package scorcerer.server;

import org.openapitools.server.apis.MatchApi
import org.openapitools.server.models.Match
import org.openapitools.server.models.MatchMatchIdScorePostRequest
import org.openapitools.server.models.Prediction

class MatchApiImpl: MatchApi {
    override fun matchListGet(filterType: String?): List<Match> {
        return listOf(
            Match("home-team", "away-tem", "match-id", 1, 0)
        )
    }

    override fun matchMatchIdPredictionsGet(matchId: String, leagueId: String?): List<Prediction> {
        return listOf(
            Prediction(2, 0, "match-id", "prediction-id", 3)
        )
    }

    override fun matchMatchIdScorePost(matchId: String, matchMatchIdScorePostRequest: MatchMatchIdScorePostRequest) {
        TODO("Not yet implemented")
    }
}