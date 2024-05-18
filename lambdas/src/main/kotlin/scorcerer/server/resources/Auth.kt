package scorcerer.server.resources

import org.http4k.core.RequestContexts
import org.openapitools.server.apis.AuthApi
import org.openapitools.server.models.LoginRequest

class Auth(context: RequestContexts) : AuthApi(context) {
    override fun login(loginRequest: LoginRequest) {
        TODO("Not yet implemented")
    }
}
