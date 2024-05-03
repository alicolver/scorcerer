package scorcerer.utils

import org.http4k.core.Response
import org.http4k.core.Status
import org.postgresql.util.PSQLException
import scorcerer.server.ApiResponseError

fun throwDatabaseError(error: PSQLException, duplicateErrorMessage: String): Nothing = throw when {
    error.message?.contains("duplicate key") == true -> ApiResponseError(Response(Status.BAD_REQUEST).body(duplicateErrorMessage))
    else -> error
}
