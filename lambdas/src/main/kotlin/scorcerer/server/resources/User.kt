package scorcerer.server.resources

import org.openapitools.server.apis.UserApi
import org.openapitools.server.models.GetUserPoints200Response
import org.openapitools.server.models.LoginRequest
import org.openapitools.server.models.Prediction

class User : UserApi() {
    override fun getUserPoints(userId: String): GetUserPoints200Response {
        TODO("Not yet implemented")
    }

    override fun getUserPredictions(userId: String, leagueId: String?): List<Prediction> {
        TODO("Not yet implemented")
    }

    override fun signup(loginRequest: LoginRequest) {
        TODO("Not yet implemented")
    }
}
