package scorcerer.server.schedule

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.models.State
import org.openapitools.server.toJson
import scorcerer.server.Environment
import scorcerer.server.db.Database
import scorcerer.server.db.tables.MatchTable
import scorcerer.server.log
import scorcerer.server.resources.getMatchDay
import scorcerer.server.resources.setScore
import scorcerer.utils.LeaderboardS3Service
import java.time.Clock
import java.time.OffsetDateTime

// Entrypoint for match starter lambda
class MatchStarter : RequestHandler<Unit, Unit> {
    private val leaderboardService = LeaderboardS3Service(S3Client { region = "eu-west-2" }, Environment.LeaderboardBucketName)

    init {
        Database.connectAndGenerateTables()
    }

    override fun handleRequest(input: Unit?, context: Context?) {
        log.info("Checking for games which have started")

        val clock = Clock.systemDefaultZone()
        // Add 1 minute to avoid missing any games which might be just starting
        val now = OffsetDateTime.now(clock).plusMinutes(1)
        log.info("Using now - $now")

        transaction {
            val matchesWhichHaveStarted = MatchTable
                .selectAll()
                .where((MatchTable.datetime less now) and (MatchTable.state eq State.UPCOMING))

            log.info("Found ${matchesWhichHaveStarted.count()} games which have already started")

            matchesWhichHaveStarted.forEach {
                val matchId = it[MatchTable.id].toString()
                log.info("Starting match ${it[MatchTable.id]}")

                val matchDay = getMatchDay(matchId)!!
                setScore(matchId, matchDay, 0, 0, leaderboardService)
            }
        }

        log.info("All required matches started")

        updateLiveMatches(leaderboardService)
    }
}

fun updateLiveMatches(leaderboardService: LeaderboardS3Service) {
    val liveMatches = transaction {
        MatchTable
            .selectAll()
            .where(MatchTable.state eq State.LIVE)
            .filter { it.getOrNull(MatchTable.fotmobMatchId) != null }
            .map { LiveMatch(it[MatchTable.id].toString(), it[MatchTable.fotmobMatchId]!!) }
    }

    val putObjectRequest = PutObjectRequest {
        bucket = leaderboardService.s3BucketName
        key = liveMatchesKey
        body = ByteStream.fromString(liveMatches.toJson())
    }

    runBlocking {
        leaderboardService.s3Client.putObject(putObjectRequest)
    }

    log.info("Updated live matches to $liveMatches")
}
