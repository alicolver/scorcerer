package org.openapitools.api

import org.openapitools.model.AuthLoginPostRequest
import org.openapitools.model.Prediction
import org.openapitools.model.UserUserIdPointsGet200Response

interface UserApiService {

    /**
     * POST /user
     * Register as a new user
     *
     * @param authLoginPostRequest  (required)
     * @return Successful response (status code 200)
     * @see UserApi#userPost
     */
    fun userPost(authLoginPostRequest: AuthLoginPostRequest): Unit

    /**
     * GET /user/{userId}/points
     * Get users points
     *
     * @param userId  (required)
     * @return Successful response (status code 200)
     * @see UserApi#userUserIdPointsGet
     */
    fun userUserIdPointsGet(userId: kotlin.String): UserUserIdPointsGet200Response

    /**
     * GET /user/{userId}/predictions
     * Get users predictions
     *
     * @param userId  (required)
     * @param leagueId  (optional)
     * @return Successful response (status code 200)
     * @see UserApi#userUserIdPredictionsGet
     */
    fun userUserIdPredictionsGet(userId: kotlin.String, leagueId: kotlin.String?): List<Prediction>
}
