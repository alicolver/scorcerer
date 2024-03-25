package scorcerer.server

import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import scorcerer.server.db.Match
import scorcerer.server.resources.createMatch
import scorcerer.server.resources.leaderboard

class Server {
    fun start() {
        httpServer.asServer(SunHttp(8000)).start().block()
    }

    init {
        Database.connect(
            "",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = ""
        )

        transaction {
            SchemaUtils.create(Match)
        }
    }
}

fun main() {
    Server().start()
}

private val httpServer = routes(leaderboard(), createMatch())

class ApiLambdaHandler : ApiGatewayV2LambdaFunction(httpServer) {
    init {
        Database.connect(
            "",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = ""
        )
    }
}