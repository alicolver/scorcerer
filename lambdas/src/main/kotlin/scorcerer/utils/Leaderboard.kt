package scorcerer.utils

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.decodeToString
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.kotshiJson
import org.openapitools.server.models.LeaderboardInner
import org.openapitools.server.models.User
import org.openapitools.server.toJson
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.db.tables.MemberTable

fun filterLeaderboardToLeague(
    globalLeaderboard: List<LeaderboardInner>,
    leagueUserIds: List<String>,
): List<LeaderboardInner> {
    val leagueUsers = globalLeaderboard.filter { it.user.userId in leagueUserIds }

    val sortedLeague =
        leagueUsers.sortedWith(compareByDescending<LeaderboardInner> { it.user.livePoints + it.user.fixedPoints }.thenBy { it.user.name })
    var currentPosition = 1
    val lastFixedPoints = sortedLeague.firstOrNull()?.user?.fixedPoints ?: 0
    val lastLivePoints = sortedLeague.firstOrNull()?.user?.livePoints ?: 0
    var lastPoints = lastLivePoints + lastFixedPoints

    val filteredLeaderboard = sortedLeague.mapIndexed { index, leaderboardInner ->
        if (index > 0 && leaderboardInner.user.fixedPoints + leaderboardInner.user.livePoints < lastPoints) {
            currentPosition = index + 1
        }
        lastPoints = leaderboardInner.user.livePoints + leaderboardInner.user.fixedPoints
        LeaderboardInner(currentPosition, leaderboardInner.user)
    }

    return filteredLeaderboard
}

fun caclulateGlobalLeaderboard(): List<LeaderboardInner> {
    val globalUsers = transaction {
        (LeagueMembershipTable innerJoin MemberTable)
            .select(
                MemberTable.id,
                MemberTable.name,
                MemberTable.fixedPoints,
                MemberTable.livePoints,
            )
            .where { LeagueMembershipTable.leagueId eq "global" }
            .map {
                User(
                    it[MemberTable.name],
                    it[MemberTable.id],
                    it[MemberTable.fixedPoints],
                    it[MemberTable.livePoints],
                )
            }
    }

    val sortedGlobalUsers =
        globalUsers.sortedWith(compareByDescending<User> { it.livePoints + it.fixedPoints }.thenBy { it.name })
    var currentPosition = 0
    var previousPoints = Int.MAX_VALUE
    val leaderboard = sortedGlobalUsers.mapIndexed { index, user ->
        if (user.livePoints + user.fixedPoints < previousPoints) {
            currentPosition = index + 1
        }
        previousPoints = user.livePoints + user.fixedPoints
        LeaderboardInner(currentPosition, user)
    }
    return leaderboard
}

class LeaderboardS3Service(private val s3Client: S3Client, private val s3BucketName: String) {
    suspend fun writeLeaderboard(leaderboard: List<LeaderboardInner>, matchDay: Int) {
        val request = PutObjectRequest {
            bucket = s3BucketName
            key = "matchDay$matchDay.json"
            body = ByteStream.fromString(leaderboard.toJson())
        }
        s3Client.putObject(request)
    }

    suspend fun getLeaderboard(matchDay: Int): List<LeaderboardInner> {
        val request = GetObjectRequest {
            bucket = s3BucketName
            key = "matchDay$matchDay.json"
        }

        val leaderboard = s3Client.getObject(request) { resp ->
            val json = resp.body?.decodeToString()
            requireNotNull(json) { "Leaderboard is empty" }
            return@getObject kotshiJson.asA<List<LeaderboardInner>>(json)
        }

        return leaderboard
    }
}