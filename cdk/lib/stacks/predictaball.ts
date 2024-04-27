import { App, Duration, SecretValue, Stack, StackProps } from "aws-cdk-lib"
import { Instance, InstanceClass, InstanceSize, InstanceType, MachineImage, Port, SubnetType, Vpc } from "aws-cdk-lib/aws-ec2"
import { Credentials, DatabaseInstance, DatabaseInstanceEngine, StorageType } from "aws-cdk-lib/aws-rds"
import { dbPassword } from "../environment"
import { Code, Function, Runtime } from "aws-cdk-lib/aws-lambda"
import { LambdaRestApi } from "aws-cdk-lib/aws-apigateway"

const dbUser = "postgres"
const dbPort = 5432

export class Predictaball extends Stack {
  constructor(scope: App, id: string, props?: StackProps) {
    super(scope, id, props)

    const vpc = Vpc.fromLookup(this, "default-vpc", { isDefault: true })

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

    const apiHandler = new Function(this, "apiHandler", {
      runtime: Runtime.JAVA_11,
      code: Code.fromAsset("../lambdas/build/distributions/scorcerer-1.0.0.zip"),
      handler: "scorcerer.server.ApiLambdaHandler",
      timeout: Duration.seconds(15),
      memorySize: 256,
      environment: {
        DB_USER: dbUser,
        DB_PASSWORD: dbPassword,
        DB_URL: db.dbInstanceEndpointAddress,
        DB_NAME: "postgres",
        DB_PORT: db.dbInstanceEndpointPort
      },
      vpc: vpc,
      allowPublicSubnet: true
    })

    db.connections.allowFrom(apiHandler, Port.tcp(dbPort))

    new LambdaRestApi(this, "apiGateway", {
      handler: apiHandler,
      proxy: true
    })
  }
}