package scorcerer.server.resources

import aws.sdk.kotlin.services.s3.S3Client
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
import org.postgresql.util.PSQLException
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.db.tables.LeagueTable
import scorcerer.server.db.tables.MemberTable
import scorcerer.utils.LeaderboardS3Service
import scorcerer.utils.filterLeaderboardToLeague
import scorcerer.utils.throwDatabaseError

class League(context: RequestContexts, private val s3Client: S3Client, private val leaderboardBucketName: String) :
    LeagueApi(context) {
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
                MemberTable.firstName,
                MemberTable.familyName,
                MemberTable.id,
                MemberTable.fixedPoints,
                MemberTable.livePoints,
            ).where { LeagueTable.id eq leagueId }.map {
                User(
                    it[MemberTable.firstName],
                    it[MemberTable.familyName],
                    it[MemberTable.id],
                    it[MemberTable.fixedPoints],
                    it[MemberTable.livePoints],
                )
            }
        }
        return League(leagueId, leagueName, users)
    }

    override fun getLeagueLeaderboard(requesterUserId: String, leagueId: String): List<LeaderboardInner> {
        // TODO: get previous matchDay leaderboard so that movement can be calculated
        val globalLeaderboard = runBlocking {
            val leaderboardService = LeaderboardS3Service(s3Client, leaderboardBucketName)
            val latestLeaderboardMatchDay = leaderboardService.getLatestLeaderboardMatchDay()
            leaderboardService.getLeaderboard(latestLeaderboardMatchDay)
        }
        if (leagueId == "global") {
            return globalLeaderboard
        }
        val leagueUsersIds = transaction {
            (LeagueMembershipTable innerJoin MemberTable).select(MemberTable.id)
                .where { LeagueMembershipTable.leagueId eq leagueId }.map {
                    it[MemberTable.id]
                }
        }
        return filterLeaderboardToLeague(globalLeaderboard, leagueUsersIds)
    }

    override fun joinLeague(requesterUserId: String, leagueId: String) {
        try {
            transaction {
                LeagueMembershipTable.insert {
                    it[this.memberId] = requesterUserId
                    it[this.leagueId] = leagueId
                }
            }
        } catch (e: Exception) {
            throw ApiResponseError(Response(Status.NOT_FOUND).body("League does not exist"))
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
