package org.openapitools.api

import org.openapitools.model.Match
import org.openapitools.model.MatchMatchIdScorePostRequest
import org.openapitools.model.Prediction
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.enums.*
import io.swagger.v3.oas.annotations.media.*
import io.swagger.v3.oas.annotations.responses.*
import io.swagger.v3.oas.annotations.security.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import org.springframework.web.bind.annotation.*
import org.springframework.validation.annotation.Validated
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.beans.factory.annotation.Autowired

import javax.validation.Valid
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Email
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

import kotlin.collections.List
import kotlin.collections.Map

@RestController
@Validated
@RequestMapping("\${api.base-path:}")
class MatchApiController(@Autowired(required = true) val service: MatchApiService) {

    @Operation(
        summary = "",
        operationId = "matchListGet",
        description = """List matches""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response", content = [Content(array = ArraySchema(schema = Schema(implementation = Match::class)))]) ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/match/list"],
        produces = ["application/json"]
    )
    fun matchListGet(@Parameter(description = "", schema = Schema(allowableValues = ["live", "future", "past"])) @Valid @RequestParam(value = "filterType", required = false) filterType: kotlin.String?): ResponseEntity<List<Match>> {
        return ResponseEntity(service.matchListGet(filterType), HttpStatus.valueOf(200))
    }

    @Operation(
        summary = "",
        operationId = "matchMatchIdPredictionsGet",
        description = """Get match predictions""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response", content = [Content(array = ArraySchema(schema = Schema(implementation = Prediction::class)))]) ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/match/{matchId}/predictions"],
        produces = ["application/json"]
    )
    fun matchMatchIdPredictionsGet(@Parameter(description = "", required = true) @PathVariable("matchId") matchId: kotlin.String,@Parameter(description = "") @Valid @RequestParam(value = "leagueId", required = false) leagueId: kotlin.String?): ResponseEntity<List<Prediction>> {
        return ResponseEntity(service.matchMatchIdPredictionsGet(matchId, leagueId), HttpStatus.valueOf(200))
    }

    @Operation(
        summary = "",
        operationId = "matchMatchIdScorePost",
        description = """Update match score""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response") ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/match/{matchId}/score"],
        consumes = ["application/json"]
    )
    fun matchMatchIdScorePost(@Parameter(description = "", required = true) @PathVariable("matchId") matchId: kotlin.String,@Parameter(description = "", required = true) @Valid @RequestBody matchMatchIdScorePostRequest: MatchMatchIdScorePostRequest): ResponseEntity<Unit> {
        return ResponseEntity(service.matchMatchIdScorePost(matchId, matchMatchIdScorePostRequest), HttpStatus.valueOf(200))
    }
}
