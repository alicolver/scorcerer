package scorcerer.server.resources

import org.http4k.core.RequestContexts
import org.openapitools.server.apis.MiscApi

class Misc(context: RequestContexts) : MiscApi(context) {
    override fun ping() {}
}
