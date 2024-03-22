package scorcerer.server.resources

import jakarta.ws.rs.Path
import org.openapitools.server.apis.UserApi
import org.openapitools.server.models.AuthLoginPostRequest
import org.openapitools.server.models.Prediction
import org.openapitools.server.models.UserUserIdPointsGet200Response

@Path("/")
class User: UserApi {
    override fun userPost(authLoginPostRequest: AuthLoginPostRequest) {
        TODO("Not yet implemented")
    }

    override fun userUserIdPointsGet(userId: String): UserUserIdPointsGet200Response {
        TODO("Not yet implemented")
    }

    override fun userUserIdPredictionsGet(userId: String, leagueId: String?): List<Prediction> {
        TODO("Not yet implemented")
    }
}