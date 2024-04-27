import { App } from "aws-cdk-lib"
import { Predictaball } from "./stacks/predictaball"
import { environment } from "./environment"

const app = new App()

new Predictaball(app, "Predictaball", {
  env: environment,
})