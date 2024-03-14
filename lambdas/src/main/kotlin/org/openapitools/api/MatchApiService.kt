package org.openapitools.api

import org.openapitools.model.Match
import org.openapitools.model.MatchMatchIdScorePostRequest
import org.openapitools.model.Prediction

interface MatchApiService {

    /**
     * GET /match/list
     * List matches
     *
     * @param filterType  (optional)
     * @return Successful response (status code 200)
     * @see MatchApi#matchListGet
     */
    fun matchListGet(filterType: kotlin.String?): List<Match>

    /**
     * GET /match/{matchId}/predictions
     * Get match predictions
     *
     * @param matchId  (required)
     * @param leagueId  (optional)
     * @return Successful response (status code 200)
     * @see MatchApi#matchMatchIdPredictionsGet
     */
    fun matchMatchIdPredictionsGet(matchId: kotlin.String, leagueId: kotlin.String?): List<Prediction>

    /**
     * POST /match/{matchId}/score
     * Update match score
     *
     * @param matchId  (required)
     * @param matchMatchIdScorePostRequest  (required)
     * @return Successful response (status code 200)
     * @see MatchApi#matchMatchIdScorePost
     */
    fun matchMatchIdScorePost(matchId: kotlin.String, matchMatchIdScorePostRequest: MatchMatchIdScorePostRequest): Unit
}
