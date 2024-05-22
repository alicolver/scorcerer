package scorcerer.resources

import io.kotlintest.shouldBe
import org.http4k.core.RequestContexts
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.openapitools.server.models.CreatePredictionRequest
import scorcerer.*
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.PredictionTable
import scorcerer.server.resources.Prediction
import java.time.OffsetDateTime
import org.openapitools.server.models.Prediction as PredictionModel

class PredictionTest : DatabaseTest() {
    @Test
    fun createPrediction() {
        givenUserExists("userId", "name")
        val homeTeamId = givenTeamExists("England")
        val awayTeamId = givenTeamExists("Scotland")
        val matchId = givenMatchExists(homeTeamId, awayTeamId, OffsetDateTime.now().plusHours(1))
        val prediction = Prediction(RequestContexts()).createPrediction(
            requesterUserId = "userId",
            CreatePredictionRequest(
                1,
                2,
                matchId,

            ),
        )

        prediction.predictionId shouldBe "1"
    }

    @Test
    fun createPredictionGivenPredictionExistsUpdates() {
        givenUserExists("userId", "name")
        val homeTeamId = givenTeamExists("England")
        val awayTeamId = givenTeamExists("Scotland")
        val matchId = givenMatchExists(homeTeamId, awayTeamId, OffsetDateTime.now().plusHours(1))
        val predictionId = givenPredictionExists(matchId, "userId", 1, 1)
        Prediction(RequestContexts()).createPrediction("userId", CreatePredictionRequest(1, 2, matchId))
        transaction {
            PredictionTable.selectAll().where { PredictionTable.id eq predictionId.toInt() }.map { row ->
                row[PredictionTable.homeScore] shouldBe 1
                row[PredictionTable.awayScore] shouldBe 2
                row[PredictionTable.matchId] shouldBe matchId.toInt()
                row[PredictionTable.memberId] shouldBe "userId"
            }
        }
    }

    @Test
    fun createPredictionGivenMatchStartedRaisesError() {
        givenUserExists("userId", "name")
        val homeTeamId = givenTeamExists("England")
        val awayTeamId = givenTeamExists("Scotland")
        val matchId = givenMatchExists(homeTeamId, awayTeamId, OffsetDateTime.now().minusHours(1))
        assertThrows<ApiResponseError> {
            Prediction(RequestContexts()).createPrediction(
                "userId",
                CreatePredictionRequest(1, 2, matchId),
            )
        }
    }

    @Test
    fun getPredictionGivenUserPredictionexists() {
        givenUserExists("userId", "name")
        val homeTeamId = givenTeamExists("England")
        val awayTeamId = givenTeamExists("Scotland")
        val matchId = givenMatchExists(homeTeamId, awayTeamId, OffsetDateTime.now().minusHours(1))
        val predictionId = givenPredictionExists(matchId, "userId", 1, 1)

        val prediction = Prediction(RequestContexts()).getPrediction("userId", matchId)

        prediction shouldBe PredictionModel(1, 1, matchId, predictionId, "userId")
    }

    @Test
    fun getPredictionGivenNoPredictionRaisesNotFound() {
        givenUserExists("userId", "name")

        assertThrows<ApiResponseError> {
            Prediction(RequestContexts()).getPrediction(
                "userId",
                "1",
            )
        }
    }
}
