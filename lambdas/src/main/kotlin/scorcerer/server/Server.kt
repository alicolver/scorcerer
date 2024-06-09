package scorcerer.server

import aws.sdk.kotlin.services.s3.S3Client
import com.squareup.moshi.JsonDataException
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.AllowAll
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters.CatchAll
import org.http4k.filter.ServerFilters.Cors
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.openapitools.server.apis.allRoutes
import scorcerer.server.db.Database
import scorcerer.server.resources.Auth
import scorcerer.server.resources.League
import scorcerer.server.resources.MatchResource
import scorcerer.server.resources.Prediction
import scorcerer.server.resources.Team
import scorcerer.server.resources.User
import scorcerer.utils.LeaderboardS3Service

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

private val requestContext = RequestContexts()

val s3Client = S3Client { region = "eu-west-2" }

private val routes = allRoutes(
    Auth(requestContext),
    League(requestContext, LeaderboardS3Service(s3Client, Environment.LeaderboardBucketName)),
    MatchResource(requestContext, LeaderboardS3Service(s3Client, Environment.LeaderboardBucketName)),
    Prediction(requestContext),
    Team(requestContext),
    User(requestContext),
)

val cors = Cors(
    CorsPolicy(
        OriginPolicy.AllowAll(),
        listOf(
            "content-type",
            "access-control-allow-origin",
            "access-control-allow-headers",
            "access-control-allow-methods",
            "access-control-allow-credentials",
            "authorization",
        ),
        Method.values().toList(),
        true,
    ),
)

private val httpServer = cors.then(loggingFilter.then(CatchAll(::handleError).then(routes)))

// Entrypoint for non auth lambda
class ApiLambdaHandler : ApiGatewayRestAuthorizerLambdaFunction(httpServer, requestContext) {
    init {
        Database.connectAndGenerateTables()
    }
}

// Entrypoint for auth lambda
class ApiAuthLambdaHandler : ApiGatewayRestAuthorizerLambdaFunction(httpServer, requestContext)
