package scorcerer.resources

import io.kotlintest.shouldBe
import org.http4k.core.RequestContexts
import org.junit.jupiter.api.Test
import org.openapitools.server.models.League
import scorcerer.*
import scorcerer.server.resources.User
import org.openapitools.server.models.User as UserModel

class UserTest : DatabaseTest() {
    @Test
    fun getUserPoints() {
        givenUserExists("userId", "name", fixedPoints = 15, livePoints = 5)
        val userPoints = User(RequestContexts()).getUserPoints("", "userId")
        userPoints.livePoints shouldBe 5
        userPoints.fixedPoints shouldBe 15
    }

    @Test
    fun getUserPredictions() {
        val userId = "userId"
        givenUserExists(userId, "name", fixedPoints = 15, livePoints = 5)
        val homeTeamId = givenTeamExists("England")
        val awayTeamId = givenTeamExists("France")
        val matchId = givenMatchExists(homeTeamId, awayTeamId)

        val predictionId = givenPredictionExists(matchId, userId, 1, 1)

        val userPredictions = User(RequestContexts()).getUserPredictions("", userId)
        userPredictions.size shouldBe 1
        userPredictions[0].predictionId shouldBe predictionId
    }

    @Test
    fun getUserLeagues() {
        givenUserExists("user1", "name1")
        givenUserExists("user2", "name2")
        givenUserExists("user3", "name3")
        givenUserExists("user4", "name4")
        givenUserExists("user5", "name5")
        givenUserExists("user6", "name6")
        givenUserExists("user7", "name7")
        givenLeagueExists("league1", "First League")
        givenLeagueExists("league2", "Second League")
        givenLeagueExists("league3", "Third League")

        givenUserInLeague("user1", "league1")
        givenUserInLeague("user5", "league1")
        givenUserInLeague("user6", "league1")
        givenUserInLeague("user7", "league1")

        givenUserInLeague("user1", "league2")
        givenUserInLeague("user3", "league2")
        givenUserInLeague("user4", "league2")
        givenUserInLeague("user5", "league2")

        givenUserInLeague("user5", "league3")
        givenUserInLeague("user6", "league3")
        givenUserInLeague("user7", "league3")

        val userLeagues = User(RequestContexts()).getUserLeagues("", "user1")
        userLeagues shouldBe listOf(
            League(
                "league1",
                "First League",
                listOf(
                    UserModel("name1", "Name", "user1", 0, 0),
                    UserModel("name5", "Name", "user5", 0, 0),
                    UserModel("name6", "Name", "user6", 0, 0),
                    UserModel("name7", "Name", "user7", 0, 0),
                ),
            ),
            League(
                "league2", "Second League",
                listOf(
                    UserModel("name1", "Name", "user1", 0, 0),
                    UserModel("name3", "Name", "user3", 0, 0),
                    UserModel("name4", "Name", "user4", 0, 0),
                    UserModel("name5", "Name", "user5", 0, 0),
                ),
            ),
        )
    }
}
