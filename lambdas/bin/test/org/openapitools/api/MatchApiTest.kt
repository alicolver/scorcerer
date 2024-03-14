package org.openapitools.api

import org.openapitools.model.Match
import org.openapitools.model.MatchMatchIdScorePostRequest
import org.openapitools.model.Prediction
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity

class MatchApiTest {

    private val service: MatchApiService = MatchApiServiceImpl()
    private val api: MatchApiController = MatchApiController(service)

    /**
     * To test MatchApiController.matchListGet
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun matchListGetTest() {
        val filterType: kotlin.String? = TODO()
        val response: ResponseEntity<List<Match>> = api.matchListGet(filterType)

        // TODO: test validations
    }

    /**
     * To test MatchApiController.matchMatchIdPredictionsGet
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun matchMatchIdPredictionsGetTest() {
        val matchId: kotlin.String = TODO()
        val leagueId: kotlin.String? = TODO()
        val response: ResponseEntity<List<Prediction>> = api.matchMatchIdPredictionsGet(matchId, leagueId)

        // TODO: test validations
    }

    /**
     * To test MatchApiController.matchMatchIdScorePost
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun matchMatchIdScorePostTest() {
        val matchId: kotlin.String = TODO()
        val matchMatchIdScorePostRequest: MatchMatchIdScorePostRequest = TODO()
        val response: ResponseEntity<Unit> = api.matchMatchIdScorePost(matchId, matchMatchIdScorePostRequest)

        // TODO: test validations
    }
}
