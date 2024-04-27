package scorcerer.server

import com.amazonaws.services.lambda.runtime.Context
import com.squareup.moshi.Moshi
import okio.buffer
import okio.source
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.serverless.ApiGatewayV2AwsHttpAdapter
import org.http4k.serverless.AppLoader
import org.http4k.serverless.AppLoaderWithContexts
import org.http4k.serverless.AwsHttpAdapter
import org.http4k.serverless.AwsLambdaEventFunction
import org.http4k.serverless.FnHandler
import org.http4k.serverless.FnLoader
import java.io.InputStream

class AuthorizerLoader(
    private val requestAdapter: AwsHttpAdapter<Map<String, Any>, Map<String, Any>>,
    private val contexts: RequestContexts,
    private val appLoader: AppLoaderWithContexts,
) : FnLoader<Context> {
    private val moshi = Moshi.Builder().build()
    private val coreFilter = ServerFilters.CatchAll().then(ServerFilters.InitialiseRequestContext(contexts))

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
}

private inline fun <reified T : Any> Moshi.asA(input: InputStream): T = adapter(T::class.java).fromJson(input.source().buffer())!!

private inline fun <reified T> Moshi.asInputStream(a: T) = adapter(T::class.java).toJson(a).byteInputStream()

const val LAMBDA_AUTHORIZER_KEY = "HTTP4K_LAMBDA_AUTHORIZER"

internal fun addLambdaAuthorizerContext(authorizer: Authorizer?, contexts: RequestContexts) =
    Filter { next ->
        {
            if (authorizer != null) {
                contexts[it][LAMBDA_AUTHORIZER_KEY] = authorizer
            }
            next(it)
        }
    }

data class Authorizer(
    val claims: Map<String, String>,
    val scopes: List<String>,
)

private fun Map<String, Any>.getAuthorizerContext(): Authorizer? {
    val jwt =
        getNested("requestContext")?.getNested("authorizer")?.getNested("jwt")
            ?: return null

    return Authorizer(
        jwt.getStringNested("claims") ?: emptyMap(),
        jwt.getStringList("scopes") ?: emptyList(),
    )
}

internal fun Map<String, Any>.getNested(name: String): Map<String, Map<String, Any>>? = get(name) as? Map<String, Map<String, Any>>

internal fun Map<String, Any>.getStringNested(name: String): Map<String, String>? = get(name) as? Map<String, String>

internal fun Map<String, Any>.getStringList(name: String): List<String>? = get(name) as? List<String>

abstract class ApiGatewayV2AuthorizerLambdaFunction(input: AppLoaderWithContexts, contexts: RequestContexts) :
    AwsLambdaEventFunction(AuthorizerLoader(ApiGatewayV2AwsHttpAdapter, contexts, input)) {
    constructor(input: AppLoader, contexts: RequestContexts) : this(AppLoaderWithContexts { env, _ -> input(env) }, contexts)
    constructor(input: HttpHandler, contexts: RequestContexts) : this(AppLoader { input }, contexts)
}
