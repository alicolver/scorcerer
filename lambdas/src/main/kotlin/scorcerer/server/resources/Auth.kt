package scorcerer.server.resources

import org.openapitools.server.apis.AuthApi
import org.openapitools.server.models.LoginRequest

class Auth : AuthApi() {
    override fun login(requesterUserId: String, loginRequest: LoginRequest) {
        TODO("Not yet implemented")
    }
}
