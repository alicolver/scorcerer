package org.openapitools.api

import org.openapitools.model.AuthLoginPostRequest

interface AuthApiService {

    /**
     * POST /auth/login
     * Login
     *
     * @param authLoginPostRequest  (required)
     * @return Successful response (status code 200)
     *         or Unauthorized (status code 401)
     * @see AuthApi#authLoginPost
     */
    fun authLoginPost(authLoginPostRequest: AuthLoginPostRequest): Unit
}
