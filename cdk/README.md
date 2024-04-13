# Predictaball CDK

## Cost
All the resources created defined *should* be included in the free tier assuming your AWS account is less than 12 months old.
However, if multiple instances of these resources are deployed to the same account we would exceed the free tier limits.
To avoid this each user should deploy to their own personal AWS account. 

## Environment

* `CDK_ACCOUNT_ID` The AWS account ID you want to deploy to
* `CDK_REGION` The AWS region you want to deploy to. Defaults to `eu-west-2`
* `CDK_DB_PASSWORD` The password for the database main user

## Useful commands

* `npm run cdk -- deploy`      This will synthesize and then deploy. Should be the only command you need
* `npm run cdk -- --profile <your_profile> deploy` You will likely need to use an AWS credentials profile
* `npm run lint` This will attempt to fix and lint errors in the code

## Accessing the bastion host
We create an EC2 instance with a public IP which allows us to connect to the DB. 
Inorder to connect you must first add a ssh keypair to the instance. 
You can do this by following this [guide](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/replacing-key-pair.html)