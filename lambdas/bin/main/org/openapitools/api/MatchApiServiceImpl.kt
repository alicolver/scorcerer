package org.openapitools.api

import org.openapitools.model.Match
import org.openapitools.model.MatchMatchIdScorePostRequest
import org.openapitools.model.Prediction
import org.springframework.stereotype.Service
@Service
class MatchApiServiceImpl : MatchApiService {

    override fun matchListGet(filterType: kotlin.String?): List<Match> {
        TODO("Implement me")
    }

    override fun matchMatchIdPredictionsGet(matchId: kotlin.String, leagueId: kotlin.String?): List<Prediction> {
        TODO("Implement me")
    }

    override fun matchMatchIdScorePost(matchId: kotlin.String, matchMatchIdScorePostRequest: MatchMatchIdScorePostRequest): Unit {
        TODO("Implement me")
    }
}
