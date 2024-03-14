package org.openapitools.api

import org.openapitools.model.AuthLoginPostRequest
import org.openapitools.model.Prediction
import org.openapitools.model.UserUserIdPointsGet200Response
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity

class UserApiTest {

    private val service: UserApiService = UserApiServiceImpl()
    private val api: UserApiController = UserApiController(service)

    /**
     * To test UserApiController.userPost
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun userPostTest() {
        val authLoginPostRequest: AuthLoginPostRequest = TODO()
        val response: ResponseEntity<Unit> = api.userPost(authLoginPostRequest)

        // TODO: test validations
    }

    /**
     * To test UserApiController.userUserIdPointsGet
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun userUserIdPointsGetTest() {
        val userId: kotlin.String = TODO()
        val response: ResponseEntity<UserUserIdPointsGet200Response> = api.userUserIdPointsGet(userId)

        // TODO: test validations
    }

    /**
     * To test UserApiController.userUserIdPredictionsGet
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun userUserIdPredictionsGetTest() {
        val userId: kotlin.String = TODO()
        val leagueId: kotlin.String? = TODO()
        val response: ResponseEntity<List<Prediction>> = api.userUserIdPredictionsGet(userId, leagueId)

        // TODO: test validations
    }
}
