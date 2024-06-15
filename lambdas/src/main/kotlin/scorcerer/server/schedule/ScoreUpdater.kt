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
data class Score(
    val current: Int,
)

data class SofaScoreResponse(
    val event: SofaScoreEvent,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SofaScoreEvent(
    val homeScore: Score,
    val awayScore: Score,
)

class ScoreUpdater : RequestHandler<Unit, Unit> {
    val client = OkHttp()
    private val endpoint = "https://www.sofascore.com/api/v1/event/"
    private val eventId = "11873897"

    override fun handleRequest(input: Unit?, context: Context?) {
        log.info("Fetching the live score")

        val request = Request(Method.GET, endpoint + eventId)
        val response = client(request)

        val score = response.body.toString().fromJson<SofaScoreResponse>()
        log.info("Fetch live score with home score (${score.event.homeScore.current}) and away score (${score.event.awayScore.current})")
    }
}
