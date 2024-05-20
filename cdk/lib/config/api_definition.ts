import * as fs from "fs"
import { ApiDefinition, InlineApiDefinition } from "aws-cdk-lib/aws-apigateway"
import * as yaml from "js-yaml"

export function importApiDefinition(
  cognitoUserPoolId: string,
  apiHandlerArn: string,
  apiAuthHandlerArn: string,
  apiGatewayRoleArn: string
): ApiDefinition {
  const spec = fs.readFileSync("../lambdas/contract/api-contract.yaml").toString()

  const modified = spec
    .replaceAll("${cognitoUserPoolId}", cognitoUserPoolId)
    .replaceAll("${apiHandlerArn}", apiHandlerArn)
    .replaceAll("${apiAuthHandlerArn}", apiAuthHandlerArn)
    .replaceAll("${apiGatewayRoleArn}", apiGatewayRoleArn)

  const parsedSpec: any = yaml.load(modified)
  const paths = parsedSpec["paths"]
  Object.keys(paths).forEach(endpoint => {
    paths[endpoint] = {
      ...paths[endpoint],
      options: {
        security: [],
        "x-amazon-apigateway-integration": {
          type: "aws_proxy",
          httpMethod: "POST",
          payloadFormatVersion: "1.0",
          credentials: apiGatewayRoleArn,
          uri: `arn:aws:apigateway:\${AWS::Region}:lambda:path/2015-03-31/functions/${apiHandlerArn}/invocations`
        }
      }
    }
  })

  return new InlineApiDefinition(parsedSpec)
}