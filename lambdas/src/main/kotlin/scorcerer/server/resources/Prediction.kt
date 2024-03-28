package scorcerer.server.resources

import org.openapitools.server.apis.PredictionApi
import org.openapitools.server.models.PredictionPost200Response
import org.openapitools.server.models.PredictionPostRequest

class Prediction: PredictionApi() {
    override fun predictionPost(predictionPostRequest: PredictionPostRequest): PredictionPost200Response {
        TODO("Not yet implemented")
    }
}