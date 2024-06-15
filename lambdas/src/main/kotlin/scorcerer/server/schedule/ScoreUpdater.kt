package scorcerer.server.schedule

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request
import org.openapitools.server.fromJson
import scorcerer.server.log

@JsonIgnoreProperties(ignoreUnknown = true)
data class Team(
    val name: String,
    val score: Int,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FotMobResponse(
    val header: FotMobHeader,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FotMobHeader(
    val teams: List<Team>,
)

class ScoreUpdater : RequestHandler<Unit, Unit> {
    val client = OkHttp()
    private val endpoint = "https://www.fotmob.com/api/matchDetails?matchId="
    private val eventId = "4043838"

    override fun handleRequest(input: Unit?, context: Context?) {
        log.info("Fetching the live score")

        val request = Request(Method.GET, endpoint + eventId)
        val response = client(request)

        log.info("Response status - ${response.status}")
        log.info("Response body length - ${response.body.length}")

        if (!response.status.successful) {
            log.info(response.bodyString())
            log.info("Response status not good, exiting")
            return
        }

        val score = response.body.toString().fromJson<FotMobResponse>()
        val homeScore = score.header.teams.first().score
        val awayScore = score.header.teams.last().score
        log.info("Home score ($homeScore) Away score ($awayScore)")
    }
}
