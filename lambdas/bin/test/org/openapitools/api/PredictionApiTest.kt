package org.openapitools.api

import org.openapitools.model.PredictionPost200Response
import org.openapitools.model.PredictionPostRequest
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity

class PredictionApiTest {

    private val service: PredictionApiService = PredictionApiServiceImpl()
    private val api: PredictionApiController = PredictionApiController(service)

    /**
     * To test PredictionApiController.predictionPost
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun predictionPostTest() {
        val predictionPostRequest: PredictionPostRequest = TODO()
        val response: ResponseEntity<PredictionPost200Response> = api.predictionPost(predictionPostRequest)

        // TODO: test validations
    }
}
