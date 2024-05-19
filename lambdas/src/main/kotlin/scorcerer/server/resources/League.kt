package scorcerer.server.resources

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import kotlinx.coroutines.runBlocking
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.LeagueApi
import org.openapitools.server.models.*
import org.openapitools.server.models.League
import org.openapitools.server.models.User
import org.openapitools.server.toJson
import org.postgresql.util.PSQLException
import scorcerer.server.ApiResponseError
import scorcerer.server.Environment
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.db.tables.LeagueTable
import scorcerer.server.db.tables.MemberTable
import scorcerer.utils.throwDatabaseError

class League(context: RequestContexts) : LeagueApi(context) {
    override fun createLeague(
        requesterUserId: String,
        createLeagueRequest: CreateLeagueRequest,
    ): CreateLeague200Response {
        val id = try {
            transaction {
                LeagueTable.insert {
                    it[this.name] = createLeagueRequest.leagueName
                    it[this.id] = createLeagueRequest.leagueName.lowercase().replace(" ", "-")
                } get LeagueTable.id
            }
        } catch (e: PSQLException) {
            throwDatabaseError(e, "League already exists")
        }

        // Add the creating user as a member of the new league
        transaction {
            LeagueMembershipTable.insert {
                it[this.memberId] = requesterUserId
                it[this.leagueId] = id
            }
        }

        return CreateLeague200Response(id)
    }

    override fun getLeague(requesterUserId: String, leagueId: String): League {
        val leagueName = transaction {
            LeagueTable.select(LeagueTable.name).where { LeagueTable.id eq leagueId }.singleOrNull()
                ?.get(LeagueTable.name)
        } ?: throw ApiResponseError(Response(Status.BAD_REQUEST).body("League does not exist"))

        val users = transaction {
            (LeagueTable innerJoin LeagueMembershipTable innerJoin MemberTable).select(
                MemberTable.name,
                MemberTable.id,
                MemberTable.fixedPoints,
                MemberTable.livePoints,
            ).where { LeagueTable.id eq leagueId }.map {
                User(
                    it[MemberTable.name],
                    it[MemberTable.id],
                    it[MemberTable.fixedPoints],
                    it[MemberTable.livePoints],
                )
            }
        }
        return League(leagueId, leagueName, users)
    }

    override fun getLeagueLeaderboard(requesterUserId: String, leagueId: String): List<LeaderboardInner> {
        val users = transaction {
            (LeagueMembershipTable innerJoin MemberTable)
                .select(
                    MemberTable.id,
                    MemberTable.name,
                    MemberTable.fixedPoints,
                    MemberTable.livePoints,
                )
                .where { LeagueMembershipTable.leagueId eq leagueId }
                .map {
                    User(
                        it[MemberTable.name],
                        it[MemberTable.id],
                        it[MemberTable.fixedPoints],
                        it[MemberTable.livePoints],
                    )
                }
        }

        val sortedUsers =
            users.sortedWith(compareByDescending<User> { it.livePoints + it.fixedPoints }.thenBy { it.name })

        var currentPosition = 0
        var previousPoints = Int.MAX_VALUE
        val leaderboard = sortedUsers.mapIndexed { index, user ->
            if (user.livePoints + user.fixedPoints < previousPoints) {
                currentPosition = index + 1
            }
            previousPoints = user.livePoints + user.fixedPoints
            LeaderboardInner(currentPosition, user)
        }
        runBlocking {
            writeLeaderboardToS3(leaderboard, 1, Environment.LeaderboardBucketName)
        }

        return leaderboard
    }

    override fun joinLeague(requesterUserId: String, leagueId: String) {
        transaction {
            LeagueMembershipTable.insert {
                it[this.memberId] = requesterUserId
                it[this.leagueId] = leagueId
            }
        }
    }

    override fun leaveLeague(requesterUserId: String, leagueId: String) {
        transaction {
            LeagueMembershipTable.deleteWhere {
                (LeagueMembershipTable.leagueId eq leagueId).and(LeagueMembershipTable.memberId eq requesterUserId)
            }
        }
    }
}

suspend fun writeLeaderboardToS3(leaderboard: List<LeaderboardInner>, matchDay: Int, bucketName: String) {
    val request = PutObjectRequest {
        bucket = bucketName
        key = "matchDay$matchDay.json"
        body = ByteStream.fromString(leaderboard.toJson())
    }

    S3Client { region = "us-east-1" }.use { s3 ->
        s3.putObject(request)
    }
}
