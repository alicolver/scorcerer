package scorcerer.server.resources

import org.openapitools.server.apis.PredictionApi
import org.openapitools.server.models.CreatePrediction200Response
import org.openapitools.server.models.CreatePredictionRequest

class Prediction : PredictionApi() {
    override fun createPrediction(createPredictionRequest: CreatePredictionRequest): CreatePrediction200Response {
        TODO("Not yet implemented")
    }
}
