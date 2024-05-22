package scorcerer.utils

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Request
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.decodeToString
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.fromJson
import org.openapitools.server.models.LeaderboardInner
import org.openapitools.server.models.Movement
import org.openapitools.server.models.User
import org.openapitools.server.toJson
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.db.tables.MemberTable
import scorcerer.server.log

fun filterLeaderboardToLeague(
    globalLeaderboard: List<LeaderboardInner>?,
    leagueUserIds: List<String>,
): List<LeaderboardInner> {
    val leagueUsers = (globalLeaderboard ?: emptyList()).filter { it.user.userId in leagueUserIds }

    val sortedLeague =
        leagueUsers.sortedWith(compareByDescending<LeaderboardInner> { it.user.livePoints + it.user.fixedPoints }.thenBy { it.user.familyName })
    var currentPosition = 1
    val lastFixedPoints = sortedLeague.firstOrNull()?.user?.fixedPoints ?: 0
    val lastLivePoints = sortedLeague.firstOrNull()?.user?.livePoints ?: 0
    var lastPoints = lastLivePoints + lastFixedPoints

    val filteredLeaderboard = sortedLeague.mapIndexed { index, leaderboardInner ->
        if (index > 0 && leaderboardInner.user.fixedPoints + leaderboardInner.user.livePoints < lastPoints) {
            currentPosition = index + 1
        }
        lastPoints = leaderboardInner.user.livePoints + leaderboardInner.user.fixedPoints
        LeaderboardInner(currentPosition, leaderboardInner.user, leaderboardInner.movement)
    }

    return filteredLeaderboard
}

fun calculateMovement(
    leaderboard: List<LeaderboardInner>,
    previousLeaderboard: List<LeaderboardInner>,
): List<LeaderboardInner> {
    val previousPositions = previousLeaderboard.associateBy { it.user.userId }
    return leaderboard.map { current ->
        val previous = previousPositions[current.user.userId]
        val movement = if (previous != null) {
            when {
                current.position < previous.position -> Movement.IMPROVED
                current.position > previous.position -> Movement.WORSENED
                else -> Movement.UNCHANGED
            }
        } else {
            Movement.UNCHANGED
        }
        current.copy(movement = movement)
    }
}

fun calculateGlobalLeaderboard(previousGlobalLeaderboard: List<LeaderboardInner>?): List<LeaderboardInner> {
    val globalUsers = transaction {
        (LeagueMembershipTable innerJoin MemberTable)
            .select(
                MemberTable.id,
                MemberTable.firstName,
                MemberTable.familyName,
                MemberTable.fixedPoints,
                MemberTable.livePoints,
            )
            .where { LeagueMembershipTable.leagueId eq "global" }
            .map {
                User(
                    it[MemberTable.firstName],
                    it[MemberTable.familyName],
                    it[MemberTable.id],
                    it[MemberTable.fixedPoints],
                    it[MemberTable.livePoints],
                )
            }
    }

    val sortedGlobalUsers =
        globalUsers.sortedWith(compareByDescending<User> { it.livePoints + it.fixedPoints }.thenBy { it.familyName })
    var currentPosition = 0
    var previousPoints = Int.MAX_VALUE
    val previousPositions = previousGlobalLeaderboard?.associateBy { it.user.userId } ?: emptyMap()

    val leaderboard = sortedGlobalUsers.mapIndexed { index, user ->
        if (user.livePoints + user.fixedPoints < previousPoints) {
            currentPosition = index + 1
        }
        previousPoints = user.livePoints + user.fixedPoints

        val previousPosition = previousPositions[user.userId]?.position ?: currentPosition
        val movement = when {
            currentPosition > previousPosition -> Movement.WORSENED
            currentPosition < previousPosition -> Movement.IMPROVED
            else -> Movement.UNCHANGED
        }

        LeaderboardInner(currentPosition, user, movement)
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

    suspend fun getLatestLeaderboardMatchDay(): Int {
        val listRequest = ListObjectsV2Request {
            bucket = s3BucketName
        }
        val listResponse = s3Client.listObjectsV2(listRequest)

        val latestMatchDay = listResponse.contents
            ?.mapNotNull { it.key?.substringAfter("matchDay")?.substringBefore(".json")?.toIntOrNull() }
            ?.maxOrNull()
            ?: 0
        return latestMatchDay
    }

    suspend fun getLeaderboard(matchDay: Int): List<LeaderboardInner>? {
        val request = GetObjectRequest {
            bucket = s3BucketName
            key = "matchDay$matchDay.json"
        }

        return try {
            s3Client.getObject(request) { resp ->
                val json = resp.body?.decodeToString()
                requireNotNull(json) { "Leaderboard is empty" }
                return@getObject json.fromJson()
            }
        } catch (e: Exception) {
            log.info("Error fetching leaderboard for matchDay $matchDay: $e")
            null
        }
    }

    suspend fun getPreviousLeaderboard(matchDay: Int): List<LeaderboardInner>? {
        return if (matchDay == 0) {
            null
        } else {
            getLeaderboard(matchDay - 1)
        }
    }

    suspend fun updateGlobalLeaderboard(matchDay: Int) {
        val previousDayLeaderboard = getPreviousLeaderboard(matchDay)
        val updatedGlobalLeaderboard = calculateGlobalLeaderboard(previousDayLeaderboard)
        writeLeaderboard(updatedGlobalLeaderboard, matchDay)
    }
}
