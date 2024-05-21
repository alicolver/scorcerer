package org.openapitools.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapper = jacksonObjectMapper()

inline fun <reified A>A.toJson(): String = mapper.writeValueAsString(this)

inline fun <reified A>String.fromJson(): A = mapper.readValue<A>(this)