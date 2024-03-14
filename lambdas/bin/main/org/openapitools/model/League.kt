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
 * @param leagueId Unique league Id
 * @param name League name
 */
data class League(

    @Schema(example = "null", required = true, description = "Unique league Id")
    @get:JsonProperty("leagueId", required = true) val leagueId: kotlin.String,

    @Schema(example = "null", required = true, description = "League name")
    @get:JsonProperty("name", required = true) val name: kotlin.String
) {

}

