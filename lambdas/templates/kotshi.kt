package org.openapitools.server

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.http4k.format.ConfigurableMoshi
import org.http4k.format.ListAdapter
import org.http4k.format.MapAdapter
import org.http4k.format.ThrowableAdapter
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import se.ansman.kotshi.KotshiJsonAdapterFactory

@KotshiJsonAdapterFactory
object ServerJsonAdapter : JsonAdapter.Factory by KotshiServerJsonAdapter

val kotshiJson = ConfigurableMoshi(
    Moshi.Builder()
        .addLast(ThrowableAdapter)
        .add(ListAdapter)
        .add(MapAdapter)
        .asConfigurable(ServerJsonAdapter)
        .withStandardMappings()
        .done()
)

inline fun <reified A: Any>A.toJson(): String = kotshiJson.asJsonString(this, A::class)