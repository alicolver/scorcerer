import { AccessLogField, AccessLogFormat } from "aws-cdk-lib/aws-apigateway";

export const gatewayLogFormat = AccessLogFormat.custom(
    JSON.stringify({
        requestId: AccessLogField.contextRequestId(),
        awsEndpointRequestId: AccessLogField.contextAwsEndpointRequestId(),
        ip: AccessLogField.contextIdentitySourceIp(),
        responseLatency: AccessLogField.contextResponseLatency(),
        integrationLatency: AccessLogField.contextIntegrationLatency(),
        httpMethod: AccessLogField.contextHttpMethod(),
        resourcePath: AccessLogField.contextResourcePath(),
        status: AccessLogField.contextStatus(),
        "error.message": AccessLogField.contextErrorMessage(),
        "error.responseType": AccessLogField.contextErrorResponseType(),
        "error.validationErrorString": AccessLogField.contextErrorValidationErrorString(),
        sub: AccessLogField.contextAuthorizerClaims("sub"),
        email: AccessLogField.contextAuthorizerClaims("email"),
        firstName: AccessLogField.contextAuthorizerClaims("given_name"),
        familyName: AccessLogField.contextAuthorizerClaims("family_name"),
        admin: AccessLogField.contextAuthorizerClaims("custom:isAdmin"),
    })
)