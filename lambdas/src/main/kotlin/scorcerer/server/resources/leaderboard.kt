package scorcerer.server.resources

import scorcerer.server.db.Match as dbMatch
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.ConfigurableMoshi
import org.http4k.format.ListAdapter
import org.http4k.format.MapAdapter
import org.http4k.format.ThrowableAdapter
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import scorcerer.server.models.LeaderboardInner
import scorcerer.server.models.Match
import scorcerer.server.models.User
import se.ansman.kotshi.KotshiJsonAdapterFactory

@KotshiJsonAdapterFactory
object MatchJsonAdapter : JsonAdapter.Factory by KotshiMatchJsonAdapter

val kotshiJson = ConfigurableMoshi(
    Moshi.Builder()
        .addLast(ThrowableAdapter)
        .add(ListAdapter)
        .add(MapAdapter)
        .asConfigurable(MatchJsonAdapter)
        .withStandardMappings()
        .done()
)

fun leaderboard(): RoutingHttpHandler {
    return "/leaderboard" bind Method.GET to {
        println("leaderboard")
        val leaderboard = LeaderboardInner(1, User("Luke", "user-1"))
        val body = kotshiJson.asJsonString(leaderboard, LeaderboardInner::class)
        Response(Status.OK).body(body)
    }
}

fun createMatch(): RoutingHttpHandler {
    return "/match" bind Method.POST to ::handleCreate
}

fun handleCreate(req: Request): Response {
    val match = try {
        kotshiJson.asA<Match>(req.bodyString())
    } catch (e: Exception) {
        println("Failed unmarshal - $e")
        return Response(Status.INTERNAL_SERVER_ERROR).body("Failed to parse request body")
    }

    val response = try {
        transaction {
            dbMatch.insertAndGetId {
                it[homeTeamName] = match!!.homeTeamName
                it[awayTeamName] = match.awayTeamName
            }
        }
    } catch (e: Exception) {
        println("Failed to create - $e")
        return Response(Status.INTERNAL_SERVER_ERROR).body("Failed to create db record")
    }

    return Response(Status.OK).body(response.value.toString())
}