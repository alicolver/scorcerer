package scorcerer.server.resources

import jakarta.ws.rs.Path
import org.openapitools.server.apis.PredictionApi
import org.openapitools.server.models.PredictionPost200Response
import org.openapitools.server.models.PredictionPostRequest

@Path("/")
class Prediction : PredictionApi {
    override fun predictionPost(predictionPostRequest: PredictionPostRequest): PredictionPost200Response {
        return PredictionPost200Response("prediction-id")
    }
}