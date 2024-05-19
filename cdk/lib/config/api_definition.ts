import * as fs from "fs"
import { ApiDefinition, InlineApiDefinition } from "aws-cdk-lib/aws-apigateway"
import * as yaml from "js-yaml"

export function importApiDefinition(
  cognitoUserPoolId: string,
  apiHandlerArn: string,
  apiAuthHandlerArn: string,
  apiGatewayRoleArn: string
): ApiDefinition {
  const spec = fs.readFileSync("build/contract.yaml").toString()

  const modified = spec
    .replaceAll("${cognitoUserPoolId}", cognitoUserPoolId)
    .replaceAll("${apiHandlerArn}", apiHandlerArn)
    .replaceAll("${apiAuthHandlerArn}", apiAuthHandlerArn)
    .replaceAll("${apiGatewayRoleArn}", apiGatewayRoleArn)

  return new InlineApiDefinition(yaml.load(modified))
}