package scorcerer.server.resources

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AuthFlowType
import aws.sdk.kotlin.services.cognitoidentityprovider.model.ConfirmForgotPasswordRequest
import aws.sdk.kotlin.services.cognitoidentityprovider.model.ForgotPasswordRequest
import aws.sdk.kotlin.services.cognitoidentityprovider.model.InitiateAuthRequest
import aws.sdk.kotlin.services.cognitoidentityprovider.model.NotAuthorizedException
import kotlinx.coroutines.runBlocking
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.openapitools.server.apis.AuthApi
import org.openapitools.server.models.Login200Response
import org.openapitools.server.models.LoginRequest
import org.openapitools.server.models.ResetPasswordConfirmRequest
import org.openapitools.server.models.ResetPasswordRequest
import scorcerer.server.ApiResponseError
import scorcerer.server.Environment
import scorcerer.server.log

class Auth(context: RequestContexts) : AuthApi(context) {
    private val cognitoClient = CognitoIdentityProviderClient { region = "eu-west-2" }

    override fun login(loginRequest: LoginRequest): Login200Response {
        val request = InitiateAuthRequest {
            authFlow = AuthFlowType.UserPasswordAuth
            clientId = Environment.CognitoUserPoolClientId
            authParameters = mapOf(
                "USERNAME" to loginRequest.email,
                "PASSWORD" to loginRequest.password,
            )
        }
        log.info("Using auth type - ${request.authFlow?.value}")

        val response = runBlocking {
            try {
                cognitoClient.initiateAuth(request)
            } catch (e: NotAuthorizedException) {
                throw ApiResponseError(Response(Status.UNAUTHORIZED).body(e.message))
            }
        }

        val result = response.authenticationResult ?: throw ApiResponseError(Response(Status.UNAUTHORIZED))

        return Login200Response(result.idToken)
    }

    override fun resetPassword(resetPasswordRequest: ResetPasswordRequest) {
        val resetRequest = ForgotPasswordRequest {
            username = resetPasswordRequest.email
            clientId = Environment.CognitoUserPoolClientId
        }

        log.info("Resetting password for email - ${resetPasswordRequest.email}")

        runBlocking {
            try {
                cognitoClient.forgotPassword(resetRequest)
            } catch (e: Exception) {
                log.error("Failed to reset users password - $e")
                throw ApiResponseError(Response(Status.BAD_REQUEST).body("Failed to reset users password"))
            }
        }
    }

    override fun resetPasswordConfirm(resetPasswordConfirmRequest: ResetPasswordConfirmRequest) {
        val confirmResetRequest = ConfirmForgotPasswordRequest {
            username = resetPasswordConfirmRequest.email
            confirmationCode = resetPasswordConfirmRequest.otp
            password = resetPasswordConfirmRequest.password
            clientId = Environment.CognitoUserPoolClientId
        }

        log.info("Confirming password reset for email - ${resetPasswordConfirmRequest.email}")

        runBlocking {
            try {
                cognitoClient.confirmForgotPassword(confirmResetRequest)
            } catch (e: Exception) {
                log.error("Failed to confirm password reset - $e")
                throw ApiResponseError(Response(Status.BAD_REQUEST).body("Failed to reset users password"))
            }
        }
    }
}
