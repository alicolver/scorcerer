package scorcerer.server

import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ServerFilters.CatchAll
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import scorcerer.server.resources.Auth
import scorcerer.server.resources.Leaderboard
import scorcerer.server.resources.League
import scorcerer.server.resources.MatchResource
import scorcerer.server.resources.Prediction
import scorcerer.server.resources.User

data class ApiResponseError(
    val response: Response
) : Exception("API failed while executing request handler and provided error response")

class Server {
    fun start() {
        CatchAll(::handleError).then(httpServer).asServer(SunHttp(8000)).start().block()
    }
}

fun main() {
    Server().start()
}

fun handleError(e: Throwable): Response {
    return when (e) {
        is ApiResponseError -> e.response
        else -> {
            println(e.message)
            e.printStackTrace()
            Response(Status.INTERNAL_SERVER_ERROR).body("The API threw an error while processing the request")
        }
    }
}

private val httpServer = CatchAll(::handleError).then(
    routes(
        Auth().routes,
        League().routes,
        MatchResource().routes,
        Leaderboard().routes,
        Prediction().routes,
        User().routes,
    )
)

class ApiLambdaHandler : ApiGatewayV2LambdaFunction(httpServer)