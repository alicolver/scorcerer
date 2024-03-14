package org.openapitools.api

import org.openapitools.model.AuthLoginPostRequest
import org.openapitools.model.Prediction
import org.openapitools.model.UserUserIdPointsGet200Response
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
class UserApiController(@Autowired(required = true) val service: UserApiService) {

    @Operation(
        summary = "",
        operationId = "userPost",
        description = """Register as a new user""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response") ]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/user"],
        consumes = ["application/json"]
    )
    fun userPost(@Parameter(description = "", required = true) @Valid @RequestBody authLoginPostRequest: AuthLoginPostRequest): ResponseEntity<Unit> {
        return ResponseEntity(service.userPost(authLoginPostRequest), HttpStatus.valueOf(200))
    }

    @Operation(
        summary = "",
        operationId = "userUserIdPointsGet",
        description = """Get users points""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response", content = [Content(schema = Schema(implementation = UserUserIdPointsGet200Response::class))]) ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/user/{userId}/points"],
        produces = ["application/json"]
    )
    fun userUserIdPointsGet(@Parameter(description = "", required = true) @PathVariable("userId") userId: kotlin.String): ResponseEntity<UserUserIdPointsGet200Response> {
        return ResponseEntity(service.userUserIdPointsGet(userId), HttpStatus.valueOf(200))
    }

    @Operation(
        summary = "",
        operationId = "userUserIdPredictionsGet",
        description = """Get users predictions""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response", content = [Content(array = ArraySchema(schema = Schema(implementation = Prediction::class)))]) ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/user/{userId}/predictions"],
        produces = ["application/json"]
    )
    fun userUserIdPredictionsGet(@Parameter(description = "", required = true) @PathVariable("userId") userId: kotlin.String,@Parameter(description = "") @Valid @RequestParam(value = "leagueId", required = false) leagueId: kotlin.String?): ResponseEntity<List<Prediction>> {
        return ResponseEntity(service.userUserIdPredictionsGet(userId, leagueId), HttpStatus.valueOf(200))
    }
}
