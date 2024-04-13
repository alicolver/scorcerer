package scorcerer.server.resources

import org.ktorm.database.Database
import org.ktorm.dsl.insertAndGenerateKey
import org.openapitools.server.apis.PredictionApi
import org.openapitools.server.models.CreatePrediction200Response
import org.openapitools.server.models.CreatePredictionRequest
import scorcerer.server.db.tables.PredictionTable

class Prediction(private val database: Database) : PredictionApi() {
    override fun createPrediction(createPredictionRequest: CreatePredictionRequest): CreatePrediction200Response {
        val id = database.insertAndGenerateKey(PredictionTable) {
            set(it.homeScore, createPredictionRequest.homeScore)
            set(it.awayScore, createPredictionRequest.awayScore)
            set(it.matchId, createPredictionRequest.matchId.toInt())
        }

        return CreatePrediction200Response(id.toString())
    }
}
