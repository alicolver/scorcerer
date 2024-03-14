package org.openapitools.api

import org.openapitools.model.PredictionPost200Response
import org.openapitools.model.PredictionPostRequest

interface PredictionApiService {

    /**
     * POST /prediction
     * Create a prediction
     *
     * @param predictionPostRequest  (required)
     * @return Successful response (status code 200)
     * @see PredictionApi#predictionPost
     */
    fun predictionPost(predictionPostRequest: PredictionPostRequest): PredictionPost200Response
}
