package scorcerer.server

import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig
import java.net.URI

class Server {
    private val serverUri: URI = URI.create("http://localhost:8080")

    private val httpServer: HttpServer = GrizzlyHttpServerFactory
        .createHttpServer(serverUri, ResourceConfig()
        .packages("scorcerer.server.resources")
    )

    fun start() {
        httpServer
    }
}

fun main(args: Array<String>) {
    Server().start()
}