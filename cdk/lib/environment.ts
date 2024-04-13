import { Environment } from "aws-cdk-lib"


const accountId: string = getEnvVarOrError("CDK_ACCOUNT_ID")
const region = process.env["CDK_REGION"] || "eu-west-2"

export const environment: Environment = {
  account: accountId,
  region: region
}

export const dbPassword: string = getEnvVarOrError("CDK_DB_PASSWORD")

function getEnvVarOrError(name: string):  string {
  const val = process.env[name]
  if (!val) throw new Error(`Environment variable ${name} not defined`)
  return val || ""
}