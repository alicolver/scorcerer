package org.openapitools.api

import org.openapitools.model.AuthLoginPostRequest
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
class AuthApiController(@Autowired(required = true) val service: AuthApiService) {

    @Operation(
        summary = "",
        operationId = "authLoginPost",
        description = """Login""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response"),
            ApiResponse(responseCode = "401", description = "Unauthorized") ],
        security = [ SecurityRequirement(name = "bearerAuth") ]
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/auth/login"],
        consumes = ["application/json"]
    )
    fun authLoginPost(@Parameter(description = "", required = true) @Valid @RequestBody authLoginPostRequest: AuthLoginPostRequest): ResponseEntity<Unit> {
        return ResponseEntity(service.authLoginPost(authLoginPostRequest), HttpStatus.valueOf(200))
    }
}
