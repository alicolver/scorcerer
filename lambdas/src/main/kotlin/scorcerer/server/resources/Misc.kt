package scorcerer.server.resources

import org.http4k.core.RequestContexts
import org.openapitools.server.apis.MiscApi

class Misc(context: RequestContexts) : MiscApi(context) {
    override fun ping() {
        // Sleep for one second to force multiple containers to start
        Thread.sleep(1000)
    }
}
