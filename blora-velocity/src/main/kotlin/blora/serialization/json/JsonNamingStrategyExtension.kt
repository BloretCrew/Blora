@file:OptIn(ExperimentalSerializationApi::class)

package blora.serialization.json

import blora.extension.convertCamelCase
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.JsonNamingStrategy

internal object InternalPointSplit : JsonNamingStrategy {

    override fun serialNameForJson(
        descriptor: SerialDescriptor,
        elementIndex: Int,
        serialName: String
    ): String = serialName.convertCamelCase('.')

    override fun toString(): String = "blora.serialization.json.InternalPointSplit"

}

val JsonNamingStrategy.Builtins.PointSplit: JsonNamingStrategy
    get() = InternalPointSplit