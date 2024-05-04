package scorcerer.server

import com.squareup.moshi.JsonDataException
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ServerFilters.CatchAll
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.serverless.ApiGatewayRestLambdaFunction
import org.openapitools.server.apis.allRoutes
import scorcerer.server.db.Database
import scorcerer.server.resources.Auth
import scorcerer.server.resources.League
import scorcerer.server.resources.MatchResource
import scorcerer.server.resources.Prediction
import scorcerer.server.resources.Team
import scorcerer.server.resources.User

data class ApiResponseError(val response: Response) : Exception("API failed while executing request handler and provided error response")

class Server {
    fun start() {
        httpServer.asServer(SunHttp(8000)).start().block()
    }
}

val loggingFilter = Filter { next -> { req -> next(req).also { log.info("${req.method} ${req.uri} ${it.status}") } } }

fun main() {
    Database.connectAndGenerateTables()

    Server().start()
}

fun handleError(e: Throwable): Response =
    when (e) {
        is ApiResponseError -> e.response
        is JsonDataException -> {
            log.error(e.stackTraceToString())
            Response(Status.BAD_REQUEST).body(e.message.toString())
        }

        else -> {
            log.error(e.stackTraceToString())
            Response(Status.INTERNAL_SERVER_ERROR).body("The API threw an error while processing the request")
        }
    }

private val routes = allRoutes(Auth(), League(), MatchResource(), Prediction(), Team(), User())

private val httpServer = loggingFilter.then(CatchAll(::handleError).then(routes))

// Entrypoint for the lambda
class ApiLambdaHandler : ApiGatewayRestLambdaFunction(httpServer) {
    init {
        Database.connectAndGenerateTables()
    }
}
