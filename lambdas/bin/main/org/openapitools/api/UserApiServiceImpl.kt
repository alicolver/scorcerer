package org.openapitools.api

import org.openapitools.model.AuthLoginPostRequest
import org.openapitools.model.Prediction
import org.openapitools.model.UserUserIdPointsGet200Response
import org.springframework.stereotype.Service
@Service
class UserApiServiceImpl : UserApiService {

    override fun userPost(authLoginPostRequest: AuthLoginPostRequest): Unit {
        TODO("Implement me")
    }

    override fun userUserIdPointsGet(userId: kotlin.String): UserUserIdPointsGet200Response {
        TODO("Implement me")
    }

    override fun userUserIdPredictionsGet(userId: kotlin.String, leagueId: kotlin.String?): List<Prediction> {
        TODO("Implement me")
    }
}
