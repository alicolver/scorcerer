package org.openapitools.api

import org.openapitools.model.PredictionPost200Response
import org.openapitools.model.PredictionPostRequest
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
class PredictionApiController(@Autowired(required = true) val service: PredictionApiService) {

    @Operation(
        summary = "",
        operationId = "predictionPost",
        description = """Create a prediction""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response", content = [Content(schema = Schema(implementation = PredictionPost200Response::class))]) ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/prediction"],
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    fun predictionPost(@Parameter(description = "", required = true) @Valid @RequestBody predictionPostRequest: PredictionPostRequest): ResponseEntity<PredictionPost200Response> {
        return ResponseEntity(service.predictionPost(predictionPostRequest), HttpStatus.valueOf(200))
    }
}
