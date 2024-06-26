package scorcerer.server.events

import aws.sdk.kotlin.services.s3.S3Client
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import org.openapitools.server.fromJson
import scorcerer.server.Environment
import scorcerer.server.db.Database
import scorcerer.server.log
import scorcerer.server.resources.endMatch
import scorcerer.server.resources.getMatchDay
import scorcerer.server.resources.setScore
import scorcerer.server.schedule.ScoreUpdate
import scorcerer.utils.LeaderboardS3Service

class ScoreUpdateEventHandler : RequestHandler<SQSEvent, Unit> {
    private val leaderboardService = LeaderboardS3Service(S3Client { region = "eu-west-2" }, Environment.LeaderboardBucketName)

    init {
        Database.connectAndGenerateTables()
    }

    override fun handleRequest(input: SQSEvent?, context: Context?) {
        log.info("Handling ${input?.records?.size} records")

        val scoreUpdates = input?.records
            ?.map { it.body.fromJson<ScoreUpdate>() }
            ?.groupBy { it.matchId }
            ?.mapValues { it.value.maxBy { it.datetime } } ?: return
        log.info("Processing ${scoreUpdates.size} updates")

        scoreUpdates.forEach {
            val scoreUpdate = it.value
            log.info("Processing $scoreUpdate")

            if (scoreUpdate.ended) {
                endMatch(scoreUpdate.matchId, scoreUpdate.homeScore, scoreUpdate.awayScore, leaderboardService)
                log.info("Match ended")
            } else {
                val matchDay = getMatchDay(scoreUpdate.matchId)!!
                setScore(scoreUpdate.matchId, matchDay, scoreUpdate.homeScore, scoreUpdate.awayScore, leaderboardService)
                log.info("Score updated")
            }
        }
    }
}
