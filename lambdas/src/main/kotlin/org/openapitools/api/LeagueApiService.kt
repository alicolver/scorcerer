package org.openapitools.api

import org.openapitools.model.League
import org.openapitools.model.LeaguePost200Response
import org.openapitools.model.LeaguePostRequest

interface LeagueApiService {

    /**
     * GET /league/{leagueId}
     * Get a league by Id
     *
     * @param leagueId  (required)
     * @return Successful response (status code 200)
     * @see LeagueApi#leagueLeagueIdGet
     */
    fun leagueLeagueIdGet(leagueId: kotlin.String): League

    /**
     * POST /league/{leagueId}/join
     * Join a league
     *
     * @param leagueId  (required)
     * @return Successful response (status code 200)
     * @see LeagueApi#leagueLeagueIdJoinPost
     */
    fun leagueLeagueIdJoinPost(leagueId: kotlin.String): Unit

    /**
     * POST /league/{leagueId}/leave
     * Leave a league
     *
     * @param leagueId  (required)
     * @return Successful response (status code 200)
     * @see LeagueApi#leagueLeagueIdLeavePost
     */
    fun leagueLeagueIdLeavePost(leagueId: kotlin.String): Unit

    /**
     * POST /league
     * Create a league
     *
     * @param leaguePostRequest  (required)
     * @return Successful response (status code 200)
     * @see LeagueApi#leaguePost
     */
    fun leaguePost(leaguePostRequest: LeaguePostRequest): LeaguePost200Response
}
