import { App, Duration, RemovalPolicy, SecretValue, Stack, StackProps } from "aws-cdk-lib"
import {
  GatewayVpcEndpointAwsService,
  Instance,
  InstanceClass,
  InstanceSize,
  InstanceType,
  MachineImage,
  Port,
  SubnetType,
  Vpc
} from "aws-cdk-lib/aws-ec2"
import { Credentials, DatabaseInstance, DatabaseInstanceEngine, StorageType } from "aws-cdk-lib/aws-rds"
import { dbPassword } from "../environment"
import { Alias, Code, Function, Runtime } from "aws-cdk-lib/aws-lambda"
import { LogGroupLogDestination, MethodLoggingLevel, SpecRestApi } from "aws-cdk-lib/aws-apigateway"
import { Cognito } from "./cognito"
import { AnyPrincipal, Effect, PolicyStatement, Role, ServicePrincipal } from "aws-cdk-lib/aws-iam"
import { importApiDefinition } from "../config/api_definition"
import { Queue } from "aws-cdk-lib/aws-sqs"
import { SqsEventSource } from "aws-cdk-lib/aws-lambda-event-sources"
import {BlockPublicAccess, Bucket, BucketEncryption, HttpMethods} from "aws-cdk-lib/aws-s3"
import { LogGroup } from "aws-cdk-lib/aws-logs";
import { gatewayLogFormat } from "../config/gateway_logs";
import { Rule, Schedule } from "aws-cdk-lib/aws-events";
import { LambdaFunction } from "aws-cdk-lib/aws-events-targets";

const dbUser = "postgres"
const dbPort = 5432

export class Predictaball extends Stack {
  constructor(scope: App, id: string, props?: StackProps) {
    super(scope, id, props)

    const vpc = Vpc.fromLookup(this, "default-vpc", { isDefault: true })

    const s3BucketAcessPoint = vpc.addGatewayEndpoint("s3Endpoint", {
      service: GatewayVpcEndpointAwsService.S3,
    })

    s3BucketAcessPoint.addToPolicy(
      new PolicyStatement({
        principals: [new AnyPrincipal()],
        actions: ["s3:*"],
        resources: ["*"],
      }),
    )

    const cognito = new Cognito(this)

    const db = new DatabaseInstance(this, "predictaballDatabase", {
      engine: DatabaseInstanceEngine.POSTGRES,
      vpc: vpc,
      vpcSubnets: {
        subnets: vpc.publicSubnets
      },
      port: dbPort,
      instanceType: InstanceType.of(InstanceClass.T3, InstanceSize.MICRO), // Free tier
      storageType: StorageType.GP2, // Free tier
      allocatedStorage: 20, // Free tier
      credentials: Credentials.fromPassword(dbUser, SecretValue.unsafePlainText(dbPassword)),
    })

    const bastion = new Instance(this, "databaseBastion", {
      instanceType: InstanceType.of(InstanceClass.T2, InstanceSize.MICRO), // Free tier
      vpc: vpc,
      vpcSubnets: {
        subnetType: SubnetType.PUBLIC,
      },
      machineImage: MachineImage.latestAmazonLinux2023(),
      associatePublicIpAddress: true // This is available on the free tier
    })

    db.connections.allowFrom(bastion, Port.tcp(dbPort))
    bastion.connections.allowFromAnyIpv4(Port.tcp(22)) // Allow ssh access

    const leaderboardBucket = new Bucket(this, "leaderboardBucket", {
      blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
      encryption: BucketEncryption.S3_MANAGED,
      enforceSSL: true,
      versioned: true,
      removalPolicy: RemovalPolicy.RETAIN
    })

    new Bucket(this, "teamFlagsBucket", {
      publicReadAccess: true,
      encryption: BucketEncryption.S3_MANAGED,
      enforceSSL: true,
      versioned: true,
      removalPolicy: RemovalPolicy.RETAIN,
      cors: [
        {
          allowedOrigins: ["*"],
          allowedMethods: [
            HttpMethods.GET
          ],
          allowedHeaders: ["*"]
        }
      ]
    })

    const userCreateDLQ = new Queue(this, "userCreationDLQ")

    const userCreationQueue = new Queue(this, "userCreationQueue", {
      deadLetterQueue: {
        queue: userCreateDLQ,
        maxReceiveCount: 3,
      }
    })

    const scoreUpdateDLQ = new Queue(this, "scoreUpdateDLQ", { fifo: true })

    const scoreUpdateQueue = new Queue(this, "scoreUpdateQueue", {
      deadLetterQueue: {
        queue: scoreUpdateDLQ,
        maxReceiveCount: 1,
      },
      fifo: true
    })

    const lambdaEnvironment = {
      DB_USER: dbUser,
      DB_PASSWORD: dbPassword,
      DB_URL: db.dbInstanceEndpointAddress,
      DB_NAME: "postgres",
      DB_PORT: db.dbInstanceEndpointPort,
      USER_POOL_CLIENT_ID: cognito.poolClient.userPoolClientId,
      USER_POOL_ID: cognito.userPool.userPoolId,
      USER_CREATION_QUEUE_URL: userCreationQueue.queueUrl,
      SCORE_UPDATE_QUEUE_URL: scoreUpdateQueue.queueUrl,
      LEADERBOARD_BUCKET_NAME: leaderboardBucket.bucketName,
      "JAVA_TOOL_OPTIONS": "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
    }

    const apiHandler = new Function(this, "apiHandler", {
      runtime: Runtime.JAVA_21,
      code: Code.fromAsset("../lambdas/build/distributions/scorcerer-1.0.0.zip"),
      handler: "scorcerer.server.ApiLambdaHandler",
      timeout: Duration.seconds(15),
      memorySize: 512,
      environment: lambdaEnvironment,
      vpc: vpc,
      allowPublicSubnet: true,
    })

    const apiAuthHandler = new Function(this, "apiAuthHandler", {
      runtime: Runtime.JAVA_21,
      code: Code.fromAsset("../lambdas/build/distributions/scorcerer-1.0.0.zip"),
      handler: "scorcerer.server.ApiAuthLambdaHandler",
      timeout: Duration.seconds(25),
      memorySize: 512,
      environment: lambdaEnvironment
    })

    const userCreationHandler = new Function(this, "userCreationHandler", {
      runtime: Runtime.JAVA_21,
      code: Code.fromAsset("../lambdas/build/distributions/scorcerer-1.0.0.zip"),
      handler: "scorcerer.server.events.UserCreationEventHandler",
      timeout: Duration.seconds(25),
      memorySize: 512,
      environment: lambdaEnvironment,
      vpc: vpc,
      allowPublicSubnet: true,
    })

    const alias = new Alias(this, "UserCreationHandlerAlias", {
      aliasName: "prod",
      version: userCreationHandler.currentVersion,
    })

    const userCreationEventSource = new SqsEventSource(userCreationQueue)
    alias.addEventSource(userCreationEventSource)

    const scoreUpdateHandler = new Function(this, "scoreUpdateHandler", {
      runtime: Runtime.JAVA_21,
      code: Code.fromAsset("../lambdas/build/distributions/scorcerer-1.0.0.zip"),
      handler: "scorcerer.server.events.ScoreUpdateEventHandler",
      timeout: Duration.seconds(25),
      memorySize: 512,
      environment: lambdaEnvironment,
      vpc: vpc,
      allowPublicSubnet: true,
    })

    const scoreUpdateEventSource = new SqsEventSource(scoreUpdateQueue)
    scoreUpdateHandler.addEventSource(scoreUpdateEventSource)

    const matchStarter = new Function(this, "matchStarter", {
      runtime: Runtime.JAVA_21,
      code: Code.fromAsset("../lambdas/build/distributions/scorcerer-1.0.0.zip"),
      handler: "scorcerer.server.schedule.MatchStarter",
      timeout: Duration.seconds(25),
      memorySize: 512,
      environment: lambdaEnvironment,
      vpc: vpc,
      allowPublicSubnet: true,
    })

    const starterRule = new Rule(this, "MatchStarterRule", {
      schedule: Schedule.cron({minute: "0"})
    })
    starterRule.addTarget(new LambdaFunction(matchStarter))

    const scoreUpdater = new Function(this, "scoreUpdater", {
      runtime: Runtime.JAVA_21,
      code: Code.fromAsset("../lambdas/build/distributions/scorcerer-1.0.0.zip"),
      handler: "scorcerer.server.schedule.ScoreUpdater",
      timeout: Duration.seconds(25),
      memorySize: 512,
      environment: lambdaEnvironment,
    })

    const scoreCheckerRule = new Rule(this, "ScoreCheckerRule", {
      schedule: Schedule.cron({minute: "*/2"})
    })
    scoreCheckerRule.addTarget(new LambdaFunction(scoreUpdater))

    scoreUpdateQueue.grantSendMessages(scoreUpdater)
    userCreationQueue.grantSendMessages(apiAuthHandler)

    apiAuthHandler.addToRolePolicy(
      new PolicyStatement({
        effect: Effect.ALLOW,
        actions: [
          "cognito-idp:AdminInitiateAuth",
          "cognito-idp:AdminCreateUser",
          "cognito-idp:AdminSetUserPassword",
          "cognito-idp:AdminDeleteUser"
        ],
        resources: [cognito.userPool.userPoolArn]
      })
    )

    leaderboardBucket.grantReadWrite(apiHandler)
    leaderboardBucket.grantReadWrite(userCreationHandler)
    leaderboardBucket.grantReadWrite(matchStarter)
    leaderboardBucket.grantReadWrite(scoreUpdateHandler)
    leaderboardBucket.grantRead(scoreUpdater)

    db.connections.allowFrom(apiHandler, Port.tcp(dbPort))
    db.connections.allowFrom(userCreationHandler, Port.tcp(dbPort))
    db.connections.allowFrom(matchStarter, Port.tcp(dbPort))
    db.connections.allowFrom(scoreUpdateHandler, Port.tcp(dbPort))

    const gatewayRole = new Role(this, "gatewayRole", {
      assumedBy: new ServicePrincipal("apigateway.amazonaws.com")
    })

    const apiDefinition = importApiDefinition(
      cognito.userPool.userPoolId,
      apiHandler.functionArn,
      apiAuthHandler.functionArn,
      gatewayRole.roleArn
    )

    apiHandler.grantInvoke(gatewayRole)
    apiAuthHandler.grantInvoke(gatewayRole)

    const gatewayAccessLogs = new LogGroup(this, "ApiGatewayLogs")

    new SpecRestApi(this, "apiGateway", {
      apiDefinition: apiDefinition,
      deployOptions: {
        accessLogDestination: new LogGroupLogDestination(gatewayAccessLogs),
        accessLogFormat: gatewayLogFormat,
        methodOptions: {
          "/*/*": {
            metricsEnabled: true,
            loggingLevel: MethodLoggingLevel.INFO,
          },
        }
      }
    })
  }
}