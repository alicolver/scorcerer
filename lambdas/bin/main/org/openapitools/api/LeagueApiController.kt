package org.openapitools.api

import org.openapitools.model.League
import org.openapitools.model.LeaguePost200Response
import org.openapitools.model.LeaguePostRequest
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
class LeagueApiController(@Autowired(required = true) val service: LeagueApiService) {

    @Operation(
        summary = "",
        operationId = "leagueLeagueIdGet",
        description = """Get a league by Id""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response", content = [Content(schema = Schema(implementation = League::class))]) ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/league/{leagueId}"],
        produces = ["application/json"]
    )
    fun leagueLeagueIdGet(@Parameter(description = "", required = true) @PathVariable("leagueId") leagueId: kotlin.String): ResponseEntity<League> {
        return ResponseEntity(service.leagueLeagueIdGet(leagueId), HttpStatus.valueOf(200))
    }

    @Operation(
        summary = "",
        operationId = "leagueLeagueIdJoinPost",
        description = """Join a league""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response") ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/league/{leagueId}/join"]
    )
    fun leagueLeagueIdJoinPost(@Parameter(description = "", required = true) @PathVariable("leagueId") leagueId: kotlin.String): ResponseEntity<Unit> {
        return ResponseEntity(service.leagueLeagueIdJoinPost(leagueId), HttpStatus.valueOf(200))
    }

    @Operation(
        summary = "",
        operationId = "leagueLeagueIdLeavePost",
        description = """Leave a league""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response") ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/league/{leagueId}/leave"]
    )
    fun leagueLeagueIdLeavePost(@Parameter(description = "", required = true) @PathVariable("leagueId") leagueId: kotlin.String): ResponseEntity<Unit> {
        return ResponseEntity(service.leagueLeagueIdLeavePost(leagueId), HttpStatus.valueOf(200))
    }

    @Operation(
        summary = "",
        operationId = "leaguePost",
        description = """Create a league""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response", content = [Content(schema = Schema(implementation = LeaguePost200Response::class))]) ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/league"],
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    fun leaguePost(@Parameter(description = "", required = true) @Valid @RequestBody leaguePostRequest: LeaguePostRequest): ResponseEntity<LeaguePost200Response> {
        return ResponseEntity(service.leaguePost(leaguePostRequest), HttpStatus.valueOf(200))
    }
}
