package scorcerer.server

import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

class Server {
    private val serverUri: URI = URI.create("http://localhost:8080")

    private val httpServer: HttpServer = GrizzlyHttpServerFactory.createHttpServer(
        serverUri,
        ResourceConfig().packages("scorcerer.server.resources")
    )

    fun start() {
        httpServer
    }
}

fun main(args: Array<String>) {
    Server().start()
}

class StreamLambdaHandler: RequestStreamHandler {
    override fun handleRequest(input: InputStream?, output: OutputStream?, context: Context?) {
        handler.proxyStream(input, output, context)
    }

    companion object {
        private val config: ResourceConfig = ResourceConfig().packages("scorcerer.server.resources")

        private val handler = JerseyLambdaContainerHandler.getAwsProxyHandler(config)
    }
}