package scorcerer.server.resources

import kotlinx.coroutines.runBlocking
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
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
import scorcerer.utils.calculateMovement
import scorcerer.utils.filterLeaderboardToLeague
import scorcerer.utils.throwDatabaseError
import kotlin.math.min

class League(
    context: RequestContexts,
    private val leaderboardService: LeaderboardS3Service,
) : LeagueApi(context) {
    override fun createLeague(
        requesterUserId: String,
        createLeagueRequest: CreateLeagueRequest,
    ): CreateLeague200Response {
        val id = try {
            transaction {
                LeagueTable.insert {
                    it[this.name] = createLeagueRequest.leagueName
                    it[this.id] = createLeagueRequest.leagueName.trim().lowercase().replace("\\s+".toRegex(), "-")
                } get LeagueTable.id
            }
        } catch (e: PSQLException) {
            throwDatabaseError(e, "League already exists")
        }

        // Add the created user as a member of the new league
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

    override fun getLeagueLeaderboard(
        requesterUserId: String,
        leagueId: String,
        pageSize: String?,
        page: String?,
    ): GetLeagueLeaderboard200Response {
        val leagueName = transaction {
            LeagueTable.select(LeagueTable.name).where { LeagueTable.id eq leagueId }.singleOrNull()
                ?.get(LeagueTable.name)
        } ?: throw ApiResponseError(Response(Status.BAD_REQUEST).body("League does not exist"))
        val (latestLeaderboardMatchDay, latestGlobalLeaderboard) = runBlocking {
            val latestMatchDay = leaderboardService.getLatestLeaderboardMatchDay()
            val latestLeaderboard = leaderboardService.getLeaderboard(latestMatchDay)
            latestMatchDay to (
                latestLeaderboard ?: throw ApiResponseError(
                    Response(Status.NOT_FOUND).body("Leaderboard does not exist"),
                )
                )
        }

        if (leagueId == "global") {
            return paginateLeaderboard(leagueName, latestGlobalLeaderboard, page, pageSize)
        }

        val leagueUsersIds = getLeagueUserIds(leagueId)
        val previousGlobalLeaderboard =
            runBlocking { leaderboardService.getPreviousLeaderboard(latestLeaderboardMatchDay) }
        val filteredLeague = filterLeaderboardToLeague(latestGlobalLeaderboard, leagueUsersIds)
        val previousFilteredLeague = filterLeaderboardToLeague(previousGlobalLeaderboard, leagueUsersIds)

        val leaderboard = calculateMovement(filteredLeague, previousFilteredLeague)
        return paginateLeaderboard(leagueName, leaderboard, page, pageSize)
    }

    override fun joinLeague(requesterUserId: String, leagueId: String) {
        transaction {
            LeagueMembershipTable
                .selectAll()
                .where { (LeagueMembershipTable.leagueId eq leagueId) and (LeagueMembershipTable.memberId eq requesterUserId) }
                .singleOrNull()
        }?.let { return }

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

private const val DEFAULT_PAGE_SIZE = "100"
private const val DEFAULT_PAGE = "1"

private fun paginateLeaderboard(leagueName: String, leaderboard: List<LeaderboardInner>, page: String?, pageSize: String?): GetLeagueLeaderboard200Response {
    val pageSizeNum =
        (pageSize ?: DEFAULT_PAGE_SIZE).toIntOrNull() ?: throw ApiResponseError(Response(Status.BAD_REQUEST).body("Invalid pageSize"))
    val pageNum =
        (page ?: DEFAULT_PAGE).toIntOrNull() ?: throw ApiResponseError(Response(Status.BAD_REQUEST).body("Invalid page"))

    val start = pageSizeNum * (pageNum - 1)
    val end = start + pageSizeNum

    if (start > leaderboard.size) throw ApiResponseError(Response(Status.BAD_REQUEST).body("Page too high"))

    return GetLeagueLeaderboard200Response(
        leagueName,
        leaderboard.subList(start, min(end, leaderboard.size)),
        nextPage = if (end < leaderboard.size) (pageNum + 1).toString() else null,
    )
}

private fun getLeagueUserIds(leagueId: String): List<String> = transaction {
    (LeagueMembershipTable innerJoin MemberTable)
        .select(MemberTable.id)
        .where { LeagueMembershipTable.leagueId eq leagueId }
        .map { it[MemberTable.id] }
}
