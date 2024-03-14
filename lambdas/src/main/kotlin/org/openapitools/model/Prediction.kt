package org.openapitools.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Email
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import javax.validation.Valid
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 
 * @param homeScore The home team final number of goals
 * @param awayScore The away team final number of goals
 * @param matchId The matchId which is being predicted
 * @param predictionId Unique predictionId
 * @param points The points earned by the prediction
 */
data class Prediction(

    @Schema(example = "null", required = true, description = "The home team final number of goals")
    @get:JsonProperty("homeScore", required = true) val homeScore: kotlin.Int,

    @Schema(example = "null", required = true, description = "The away team final number of goals")
    @get:JsonProperty("awayScore", required = true) val awayScore: kotlin.Int,

    @Schema(example = "null", required = true, description = "The matchId which is being predicted")
    @get:JsonProperty("matchId", required = true) val matchId: kotlin.String,

    @Schema(example = "null", required = true, description = "Unique predictionId")
    @get:JsonProperty("predictionId", required = true) val predictionId: kotlin.String,

    @Schema(example = "null", description = "The points earned by the prediction")
    @get:JsonProperty("points") val points: kotlin.Int? = null
) {

}

