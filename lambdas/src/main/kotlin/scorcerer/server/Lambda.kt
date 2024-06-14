package scorcerer.server

import com.amazonaws.services.lambda.runtime.Context
import com.squareup.moshi.Moshi
import okio.buffer
import okio.source
import org.crac.Core
import org.crac.Resource
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.serverless.ApiGatewayRestAwsHttpAdapter
import org.http4k.serverless.AppLoader
import org.http4k.serverless.AppLoaderWithContexts
import org.http4k.serverless.AwsHttpAdapter
import org.http4k.serverless.AwsLambdaEventFunction
import org.http4k.serverless.FnHandler
import org.http4k.serverless.FnLoader
import org.openapitools.server.apis.Authorizer
import org.openapitools.server.apis.LAMBDA_AUTHORIZER_KEY
import scorcerer.server.db.Database
import java.io.InputStream

class AuthorizerLoader(
    private val requestAdapter: AwsHttpAdapter<Map<String, Any>, Map<String, Any>>,
    private val contexts: RequestContexts,
    private val appLoader: AppLoaderWithContexts,
) : FnLoader<Context>, Resource {
    private val moshi = Moshi.Builder().build()
    private val coreFilter = ServerFilters.CatchAll().then(ServerFilters.InitialiseRequestContext(contexts))

    init {
        Core.getGlobalContext().register(this)
    }

    override fun invoke(env: Map<String, String>): FnHandler<InputStream, Context, InputStream> {
        val app = appLoader(env, contexts)
        return FnHandler { inputStream, ctx ->
            val input: Map<String, Any> = moshi.asA(inputStream)
            val authorizer = input.getAuthorizerContext()
            val newRequest = requestAdapter(input, ctx)
            moshi.asInputStream(
                requestAdapter(
                    coreFilter
                        .then(addLambdaAuthorizerContext(authorizer, contexts))
                        .then(app)(newRequest),
                ),
            )
        }
    }

    override fun beforeCheckpoint(p0: org.crac.Context<out Resource>?) {
        log.info("Cooling down before snap start")
    }

    override fun afterRestore(p0: org.crac.Context<out Resource>?) {
        log.info("Snap Start")
        Database.connectAndGenerateTables()
    }
}

private inline fun <reified T : Any> Moshi.asA(input: InputStream): T = adapter(T::class.java).fromJson(input.source().buffer())!!

private inline fun <reified T> Moshi.asInputStream(a: T) = adapter(T::class.java).toJson(a).byteInputStream()

internal fun addLambdaAuthorizerContext(authorizer: Authorizer?, contexts: RequestContexts) =
    Filter { next ->
        {
            if (authorizer != null) {
                contexts[it][LAMBDA_AUTHORIZER_KEY] = authorizer
            }
            next(it)
        }
    }

private fun Map<String, Any>.getAuthorizerContext(): Authorizer? {
    val authorizer = getNested("requestContext")?.getNested("authorizer") ?: return null

    return Authorizer(
        authorizer.getStringNested("claims") ?: emptyMap(),
        authorizer.getStringList("scopes") ?: emptyList(),
    )
}

internal fun Map<String, Any>.getNested(name: String): Map<String, Map<String, Any>>? = get(name) as? Map<String, Map<String, Any>>

internal fun Map<String, Any>.getStringNested(name: String): Map<String, String>? = get(name) as? Map<String, String>

internal fun Map<String, Any>.getStringList(name: String): List<String>? = get(name) as? List<String>

abstract class ApiGatewayRestAuthorizerLambdaFunction(input: AppLoaderWithContexts, contexts: RequestContexts) :
    AwsLambdaEventFunction(AuthorizerLoader(ApiGatewayRestAwsHttpAdapter, contexts, input)) {
    constructor(input: AppLoader, contexts: RequestContexts) : this(AppLoaderWithContexts { env, _ -> input(env) }, contexts)
    constructor(input: HttpHandler, contexts: RequestContexts) : this(AppLoader { input }, contexts)
}
