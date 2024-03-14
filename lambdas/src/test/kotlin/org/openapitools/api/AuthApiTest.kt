package org.openapitools.api

import org.openapitools.model.AuthLoginPostRequest
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity

class AuthApiTest {

    private val service: AuthApiService = AuthApiServiceImpl()
    private val api: AuthApiController = AuthApiController(service)

    /**
     * To test AuthApiController.authLoginPost
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun authLoginPostTest() {
        val authLoginPostRequest: AuthLoginPostRequest = TODO()
        val response: ResponseEntity<Unit> = api.authLoginPost(authLoginPostRequest)

        // TODO: test validations
    }
}
