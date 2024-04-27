package scorcerer.server.resources

import org.http4k.core.Response
import org.http4k.core.Status
import org.ktorm.database.Database
import org.ktorm.dsl.insertAndGenerateKey
import org.openapitools.server.apis.PredictionApi
import org.openapitools.server.models.CreatePrediction200Response
import org.openapitools.server.models.CreatePredictionRequest
import org.postgresql.util.PSQLException
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.PredictionTable

class Prediction(private val database: Database) : PredictionApi() {
    override fun createPrediction(requesterUserId: String, createPredictionRequest: CreatePredictionRequest): CreatePrediction200Response {
        val id = try {
            database.insertAndGenerateKey(PredictionTable) {
                set(it.homeScore, createPredictionRequest.homeScore)
                set(it.awayScore, createPredictionRequest.awayScore)
                set(it.matchId, createPredictionRequest.matchId.toInt())
//                TODO: regenerate API spec with correct types
//                set(it.memberId, requesterUserId)
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
