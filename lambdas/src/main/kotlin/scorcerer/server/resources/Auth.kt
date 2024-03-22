package scorcerer.server.resources

import jakarta.ws.rs.Path
import org.openapitools.server.apis.AuthApi
import org.openapitools.server.models.AuthLoginPostRequest

@Path("/")
class Auth: AuthApi {
    override fun authLoginPost(authLoginPostRequest: AuthLoginPostRequest) {
        TODO("Not yet implemented")
    }
}