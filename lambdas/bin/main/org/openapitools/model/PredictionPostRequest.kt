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
 */
data class PredictionPostRequest(

    @Schema(example = "null", description = "The home team final number of goals")
    @get:JsonProperty("homeScore") val homeScore: kotlin.Int? = null,

    @Schema(example = "null", description = "The away team final number of goals")
    @get:JsonProperty("awayScore") val awayScore: kotlin.Int? = null,

    @Schema(example = "null", description = "The matchId which is being predicted")
    @get:JsonProperty("matchId") val matchId: kotlin.String? = null
) {

}

