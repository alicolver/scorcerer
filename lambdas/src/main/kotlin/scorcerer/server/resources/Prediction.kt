package scorcerer.server.resources

import org.http4k.core.Response
import org.http4k.core.Status
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.PredictionApi
import org.openapitools.server.models.CreatePrediction200Response
import org.openapitools.server.models.CreatePredictionRequest
import org.postgresql.util.PSQLException
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.PredictionTable

class Prediction() : PredictionApi() {
    override fun createPrediction(
        requesterUserId: String,
        createPredictionRequest: CreatePredictionRequest,
    ): CreatePrediction200Response {
        val id = try {
            transaction {
                PredictionTable.insert {
                    it[this.userId] = requesterUserId
                    it[this.matchId] = createPredictionRequest.matchId
                    it[this.homeScore] = createPredictionRequest.homeScore
                    it[this.awayScore] = createPredictionRequest.awayScore
                } get PredictionTable.id
            }
        } catch (e: PSQLException) {
            if (e.message?.contains("duplicate key") == true) {
                throw ApiResponseError(Response(Status.BAD_REQUEST).body("Prediction already exists, use updatePrediction instead"))
            } else {
                throw e
            }
        }

        return CreatePrediction200Response(id.toString())
    }
}
