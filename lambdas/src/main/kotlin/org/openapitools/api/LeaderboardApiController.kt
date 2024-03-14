package org.openapitools.api

import org.openapitools.model.LeaderboardInner
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
class LeaderboardApiController(@Autowired(required = true) val service: LeaderboardApiService) {

    @Operation(
        summary = "",
        operationId = "leaderboardGet",
        description = """Returns the leaderboard""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response", content = [Content(array = ArraySchema(schema = Schema(implementation = LeaderboardInner::class)))]) ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/leaderboard"],
        produces = ["application/json"]
    )
    fun leaderboardGet(@Parameter(description = "") @Valid @RequestParam(value = "leagueId", required = false) leagueId: kotlin.String?): ResponseEntity<List<LeaderboardInner>> {
        return ResponseEntity(service.leaderboardGet(leagueId), HttpStatus.valueOf(200))
    }
}
