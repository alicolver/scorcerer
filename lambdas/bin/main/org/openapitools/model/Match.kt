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
 * @param homeTeam Home team
 * @param awayTeam Away team
 * @param matchId Unique matchId
 * @param homeScore The home team final number of goals
 * @param awayScore The away team final number of goals
 */
data class Match(

    @Schema(example = "null", required = true, description = "Home team")
    @get:JsonProperty("homeTeam", required = true) val homeTeam: kotlin.String,

    @Schema(example = "null", required = true, description = "Away team")
    @get:JsonProperty("awayTeam", required = true) val awayTeam: kotlin.String,

    @Schema(example = "null", required = true, description = "Unique matchId")
    @get:JsonProperty("matchId", required = true) val matchId: kotlin.String,

    @Schema(example = "null", description = "The home team final number of goals")
    @get:JsonProperty("homeScore") val homeScore: kotlin.Int? = null,

    @Schema(example = "null", description = "The away team final number of goals")
    @get:JsonProperty("awayScore") val awayScore: kotlin.Int? = null
) {

}

