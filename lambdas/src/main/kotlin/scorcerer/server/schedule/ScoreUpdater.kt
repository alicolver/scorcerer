package scorcerer.server.schedule

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.model.SendMessageRequest
import aws.smithy.kotlin.runtime.content.decodeToString
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.coroutines.runBlocking
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request
import org.openapitools.server.fromJson
import org.openapitools.server.toJson
import scorcerer.server.Environment
import scorcerer.server.log
import java.time.Clock
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class Team(
    val name: String,
    val score: Int,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FotMobResponse(
    val header: FotMobHeader,
    val general: FotMobGeneral,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FotMobHeader(
    val teams: List<Team>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FotMobGeneral(
    val started: Boolean,
    val finished: Boolean,
    val matchName: String,
)

data class LiveMatch(
    val matchId: String,
    val fotmobMatchId: String,
)

data class ScoreUpdate(
    val matchId: String,
    val homeScore: Int,
    val awayScore: Int,
    val datetime: Instant,
)

val liveMatchesKey = "live-matches.json"

class ScoreUpdater : RequestHandler<Unit, Unit> {
    private val client = OkHttp()
    private val s3Client = S3Client { region = "eu-west-2" }
    private val sqsClient = SqsClient { region = "eu-west-2" }

    private val endpoint = "https://www.fotmob.com/api/matchDetails?matchId="

    override fun handleRequest(input: Unit?, context: Context?) {
        log.info("Fetching the live matches from S3")

        val getObjectRequest = GetObjectRequest {
            bucket = Environment.LeaderboardBucketName
            key = liveMatchesKey
        }

        val liveMatches = runBlocking {
            s3Client.getObject(getObjectRequest) { it.body?.decodeToString()?.fromJson<List<LiveMatch>>() }
        }

        if (liveMatches.isNullOrEmpty()) {
            log.info("No live matches")
            return
        }

        liveMatches.forEach {
            log.info("Fetching for $it")
            val request = Request(Method.GET, endpoint + it.fotmobMatchId)
            val response = client(request)

            log.info("Response status - ${response.status}")
            log.info("Response body length - ${response.body.length}")

            if (!response.status.successful) {
                log.info(response.bodyString())
                log.info("Response status not good, exiting")
                return
            }

            val fotmobResponse = response.body.toString().fromJson<FotMobResponse>()

            log.info(fotmobResponse.general.matchName)

            if (!fotmobResponse.general.started || fotmobResponse.general.finished) {
                log.info("Match is not live")
                return
            }
            val now = Clock.systemDefaultZone().instant()

            val homeScore = fotmobResponse.header.teams.first().score
            val awayScore = fotmobResponse.header.teams.last().score
            log.info("Home score ($homeScore) Away score ($awayScore) for matchId (${it.matchId})")

            runBlocking {
                sqsClient.sendMessage(
                    SendMessageRequest {
                        queueUrl = Environment.ScoreUpdateQueueUrl
                        messageBody = ScoreUpdate(it.matchId, homeScore, awayScore, now).toJson()
                        messageGroupId = it.matchId
                        messageDeduplicationId = UUID.randomUUID().toString()
                    },
                )
            }
        }
    }
}
