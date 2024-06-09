import { Duration, Stack } from "aws-cdk-lib"
import { StringAttribute, UserPool, UserPoolClient } from "aws-cdk-lib/aws-cognito"

export class Cognito {

  readonly userPool: UserPool
  readonly poolClient: UserPoolClient

  constructor(scope: Stack) {
    this.userPool = new UserPool(scope, "userPool", {
      selfSignUpEnabled: true,
      autoVerify: {
          email: true
      },
      userPoolName: "predictaballUserPool",
      customAttributes: {
        givenName: new StringAttribute({ mutable: false }),
        familyName: new StringAttribute({ mutable: false }),
        email: new StringAttribute({ mutable: false }),
        isAdmin: new StringAttribute({ mutable: true }),
      },
      passwordPolicy: {
        minLength: 6,
        requireLowercase: true,
        requireDigits: true,
        requireUppercase: false,
        requireSymbols: false,
      },
    })
        
    this.poolClient = new UserPoolClient(scope, "predictaballUserClient", {
      userPool: this.userPool,
      authFlows: {
        adminUserPassword: true,
        userPassword: true,
        userSrp: true
      },
      accessTokenValidity: Duration.hours(24),
      idTokenValidity: Duration.hours(24),
      refreshTokenValidity: Duration.days(30),
      userPoolClientName: "predictaballUserClient",
    })
  }
}