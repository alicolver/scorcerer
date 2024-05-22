package scorcerer.utils

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.*
import aws.smithy.kotlin.runtime.content.decodeToString
import io.kotest.assertions.any
import io.kotlintest.shouldBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openapitools.server.models.LeaderboardInner
import org.openapitools.server.models.Movement
import org.openapitools.server.models.User
import org.openapitools.server.toJson

class TestLeaderboardS3Service {
    private lateinit var s3Client: S3Client
    private lateinit var leaderboardS3Service: LeaderboardS3Service
    private val s3BucketName = "test-bucket"

    @BeforeEach
    fun setUp() {
        s3Client = mockk()
        leaderboardS3Service = LeaderboardS3Service(s3Client, s3BucketName)
    }

    @Test
    fun writeLeaderboardShouldCallPutObjectWithCorrectParameters() = runBlocking {
        val leaderboard = listOf(
            LeaderboardInner(1, User("name", "secondName1", "user1", 10, 5), Movement.IMPROVED),
            LeaderboardInner(1, User("name", "secondName2", "user2", 10, 5), Movement.IMPROVED),
        )

        val expectedKey = "matchDay1.json"
        val expectedBody = leaderboard.toJson()

        coEvery { s3Client.putObject(any<PutObjectRequest>()) } returns mockk()
        val putObjectRequestSlot = slot<PutObjectRequest>()

        leaderboardS3Service.writeLeaderboard(leaderboard, 1)

        coVerify {
            s3Client.putObject(
                capture(putObjectRequestSlot),
            )
        }
        val capturedRequest = putObjectRequestSlot.captured
        capturedRequest.bucket shouldBe s3BucketName
        capturedRequest.key shouldBe expectedKey
        val capturedBody = capturedRequest.body?.decodeToString()
        capturedBody shouldBe expectedBody
    }

    @Test
    fun testGetLatestLeaderboardMatchDayGivenItemsInBucket() {
        runBlocking {
            val s3Objects = listOf(
                Object { key = "matchDay1.json" },
                Object { key = "matchDay2.json" },
                Object { key = "matchDay3.json" },
            )
            val listResponse = ListObjectsV2Response {
                contents = s3Objects
            }

            coEvery { s3Client.listObjectsV2(any<ListObjectsV2Request>()) } returns listResponse

            val latestMatchDay = leaderboardS3Service.getLatestLeaderboardMatchDay()

            latestMatchDay shouldBe 3
            coVerify { s3Client.listObjectsV2(any<ListObjectsV2Request>()) }
        }
    }

    @Test
    fun testGetLatestLeaderboardMatchDayGivenNothingInBucket() {
        runBlocking {
            val s3Objects = emptyList<Object>()
            val listResponse = ListObjectsV2Response {
                contents = s3Objects
            }

            coEvery { s3Client.listObjectsV2(any<ListObjectsV2Request>()) } returns listResponse

            val latestMatchDay = leaderboardS3Service.getLatestLeaderboardMatchDay()

            latestMatchDay shouldBe -1
            coVerify { s3Client.listObjectsV2(any<ListObjectsV2Request>()) }
        }
    }

    @Test
    fun getLeaderboardShouldReturnLeaderboardForAValidMatchDay() = runBlocking {
        val matchDay = 1
        val exception = RuntimeException("Some S3 error")

        coEvery { s3Client.getObject(any<GetObjectRequest>(), any<suspend (GetObjectResponse) -> List<LeaderboardInner>?>()) } throws exception

        val result = leaderboardS3Service.getLeaderboard(matchDay)

        result shouldBe null
        coVerify { s3Client.getObject(any<GetObjectRequest>(), any()) }
    }
}

class LeaderboardTest {
    @Test
    fun testFilterLeaderboardToLeague() {
        val globalLeagueLeaderboard = listOf(
            LeaderboardInner(1, User("name", "secondName5", "user5", 10, 5), Movement.IMPROVED),
            LeaderboardInner(2, User("name", "secondName3", "user3", 5, 7), Movement.IMPROVED),
            LeaderboardInner(3, User("name", "secondName1", "user1", 5, 5), Movement.UNCHANGED),
            LeaderboardInner(3, User("name", "secondName4", "user4", 5, 5), Movement.IMPROVED),
            LeaderboardInner(5, User("name", "secondName2", "user2", 3, 4), Movement.WORSENED),
        )

        val leagueUserIds = listOf("user1", "user2", "user4", "user5")

        val filteredLeaderboard = filterLeaderboardToLeague(globalLeagueLeaderboard, leagueUserIds)
        filteredLeaderboard shouldBe listOf(
            LeaderboardInner(1, User("name", "secondName5", "user5", 10, 5), Movement.IMPROVED),
            LeaderboardInner(2, User("name", "secondName1", "user1", 5, 5), Movement.UNCHANGED),
            LeaderboardInner(2, User("name", "secondName4", "user4", 5, 5), Movement.IMPROVED),
            LeaderboardInner(4, User("name", "secondName2", "user2", 3, 4), Movement.WORSENED),
        )
    }

    @Test
    fun testCalculateMovement() {
        val previousLeaderboard = listOf(
            LeaderboardInner(1, User("name", "secondName5", "user5", 10, 5), Movement.UNCHANGED),
            LeaderboardInner(2, User("name", "secondName3", "user3", 5, 7), Movement.UNCHANGED),
            LeaderboardInner(3, User("name", "secondName1", "user1", 5, 5), Movement.UNCHANGED),
            LeaderboardInner(3, User("name", "secondName4", "user4", 5, 5), Movement.UNCHANGED),
        )

        val leaderboard = listOf(
            LeaderboardInner(1, User("name", "secondName1", "user1", 5, 5), Movement.UNCHANGED),
            LeaderboardInner(1, User("name", "secondName4", "user4", 5, 5), Movement.UNCHANGED),
            LeaderboardInner(3, User("name", "secondName3", "user3", 5, 7), Movement.UNCHANGED),
            LeaderboardInner(4, User("name", "secondName5", "user5", 10, 5), Movement.UNCHANGED),
            LeaderboardInner(5, User("name", "secondName2", "user2", 0, 0), Movement.UNCHANGED),
        )

        val leaderboardWithMovementRecalculated = calculateMovement(leaderboard, previousLeaderboard)
        leaderboardWithMovementRecalculated shouldBe listOf(
            LeaderboardInner(1, User("name", "secondName1", "user1", 5, 5), Movement.IMPROVED),
            LeaderboardInner(1, User("name", "secondName4", "user4", 5, 5), Movement.IMPROVED),
            LeaderboardInner(3, User("name", "secondName3", "user3", 5, 7), Movement.WORSENED),
            LeaderboardInner(4, User("name", "secondName5", "user5", 10, 5), Movement.WORSENED),
            LeaderboardInner(5, User("name", "secondName2", "user2", 0, 0), Movement.UNCHANGED),
        )
    }
}
