package scorcerer.server.events

import aws.sdk.kotlin.services.s3.S3Client
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.kotshiJson
import scorcerer.server.Environment
import scorcerer.server.db.Database
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.db.tables.LeagueTable
import scorcerer.server.db.tables.MemberTable
import scorcerer.server.log
import scorcerer.utils.LeaderboardS3Service
import scorcerer.utils.calculateGlobalLeaderboard
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class UserCreationEvent(
    val id: String,
    val firstName: String,
    val familyName: String,
)

class UserCreationEventHandler : RequestHandler<SQSEvent, Unit> {
    init {
        Database.connectAndGenerateTables()
    }

    override fun handleRequest(input: SQSEvent?, context: Context?) {
        log.info("Handling ${input?.records?.size} records")

        input?.records?.forEach {
            val userCreationEvent = kotshiJson.asA<UserCreationEvent>(it.body)

            transaction {
                MemberTable.insert { row ->
                    row[this.id] = userCreationEvent.id
                    row[this.firstName] = userCreationEvent.firstName
                    row[this.familyName] = userCreationEvent.familyName
                    row[this.fixedPoints] = 0
                    row[this.livePoints] = 0
                }

                val globalLeagueExists = LeagueTable.selectAll().where { LeagueTable.id eq "global" }.count() > 0
                if (!globalLeagueExists) {
                    LeagueTable.insert {
                        it[this.name] = "Global"
                        it[this.id] = "global"
                    }
                }

                LeagueMembershipTable.insert {
                    it[this.memberId] = userCreationEvent.id
                    it[this.leagueId] = "global"
                }
            }

            val globalLeaderboard = calculateGlobalLeaderboard()
            val s3Client = S3Client { region = "eu-west-2" }
            runBlocking {
                val leaderboardService = LeaderboardS3Service(s3Client, Environment.LeaderboardBucketName)
                val latestLeaderboardMatchDay = leaderboardService.getLatestLeaderboardMatchDay()
                leaderboardService.writeLeaderboard(globalLeaderboard, latestLeaderboardMatchDay)
            }
        }
    }
}
