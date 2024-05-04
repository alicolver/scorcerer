package scorcerer.resources

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import scorcerer.DatabaseTest
import scorcerer.givenMatchExists
import scorcerer.givenPredictionExists
import scorcerer.givenTeamExists
import scorcerer.givenUserExists
import scorcerer.server.resources.User

class UserTest : DatabaseTest() {
    @Test
    fun getUserPoints() {
        givenUserExists("userId", "name", 15, 5)
        val userPoints = User().getUserPoints("", "userId")
        userPoints.livePoints shouldBe 5
        userPoints.fixedPoints shouldBe 15
    }

    @Test
    fun getUserPredictions() {
        val userId = "userId"
        givenUserExists(userId, "name", 15, 5)
        val homeTeamId = givenTeamExists("England")
        val awayTeamId = givenTeamExists("France")
        val matchId = givenMatchExists(homeTeamId, awayTeamId)

        val predictionId = givenPredictionExists(matchId, userId, 1, 1)

        val userPredictions = User().getUserPredictions("", userId)
        userPredictions.size shouldBe 1
        userPredictions[0].predictionId shouldBe predictionId
    }
}
