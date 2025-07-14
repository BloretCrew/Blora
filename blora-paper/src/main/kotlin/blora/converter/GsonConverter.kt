package blora.converter

import kotlinx.serialization.json.*
import com.google.gson.JsonArray as GsonArray
import com.google.gson.JsonElement as GsonElement
import com.google.gson.JsonNull as GsonNull
import com.google.gson.JsonObject as GsonObject
import com.google.gson.JsonPrimitive as GsonPrimitive

fun convertGsonToKotlinxSerialization(element: GsonElement): JsonElement {
    return when (element) {
        is GsonNull -> JsonNull
        is GsonObject -> convertGsonObject(element)
        is GsonArray -> convertGsonArray(element)
        is GsonPrimitive -> convertGsonPrimitive(element)
        else -> throw IllegalArgumentException("Unsupported Gson element type: ${element::class.java}")
    }
}

private fun convertGsonObject(gsonObject: GsonObject): JsonObject {
    return JsonObject(
        gsonObject.keySet()
            .associateWith { key -> convertGsonToKotlinxSerialization(gsonObject.get(key)) })
}

private fun convertGsonArray(gsonArray: GsonArray): JsonArray {
    return JsonArray(gsonArray.map { convertGsonToKotlinxSerialization(it) })
}

private fun convertGsonPrimitive(gsonPrimitive: GsonPrimitive): JsonPrimitive {
    return when {
        gsonPrimitive.isBoolean -> JsonPrimitive(gsonPrimitive.asBoolean)
        gsonPrimitive.isNumber -> handleGsonNumber(gsonPrimitive)
        else -> JsonPrimitive(gsonPrimitive.asString)
    }
}

private fun handleGsonNumber(primitive: GsonPrimitive): JsonPrimitive {
    val str = primitive.asString
    return when {
        '.' in str || 'e' in str || 'E' in str ->
            str.toDoubleOrNull()?.let(::JsonPrimitive) ?: JsonPrimitive(str)

        else ->
            str.toLongOrNull()?.let(::JsonPrimitive) ?: JsonPrimitive(str)
    }
}