package org.openapitools.api

import org.openapitools.model.League
import org.openapitools.model.LeaguePost200Response
import org.openapitools.model.LeaguePostRequest
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity

class LeagueApiTest {

    private val service: LeagueApiService = LeagueApiServiceImpl()
    private val api: LeagueApiController = LeagueApiController(service)

    /**
     * To test LeagueApiController.leagueLeagueIdGet
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun leagueLeagueIdGetTest() {
        val leagueId: kotlin.String = TODO()
        val response: ResponseEntity<League> = api.leagueLeagueIdGet(leagueId)

        // TODO: test validations
    }

    /**
     * To test LeagueApiController.leagueLeagueIdJoinPost
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun leagueLeagueIdJoinPostTest() {
        val leagueId: kotlin.String = TODO()
        val response: ResponseEntity<Unit> = api.leagueLeagueIdJoinPost(leagueId)

        // TODO: test validations
    }

    /**
     * To test LeagueApiController.leagueLeagueIdLeavePost
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun leagueLeagueIdLeavePostTest() {
        val leagueId: kotlin.String = TODO()
        val response: ResponseEntity<Unit> = api.leagueLeagueIdLeavePost(leagueId)

        // TODO: test validations
    }

    /**
     * To test LeagueApiController.leaguePost
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun leaguePostTest() {
        val leaguePostRequest: LeaguePostRequest = TODO()
        val response: ResponseEntity<LeaguePost200Response> = api.leaguePost(leaguePostRequest)

        // TODO: test validations
    }
}
