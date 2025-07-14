package blora.converter

import kotlinx.serialization.json.*
import com.google.gson.JsonArray as GsonArray
import com.google.gson.JsonElement as GsonElement
import com.google.gson.JsonNull as GsonNull
import com.google.gson.JsonObject as GsonObject
import com.google.gson.JsonPrimitive as GsonPrimitive

fun convertKotlinxToGson(element: JsonElement): GsonElement {
    return when (element) {
        is JsonNull -> GsonNull.INSTANCE
        is JsonObject -> convertKotlinxObject(element)
        is JsonArray -> convertKotlinxArray(element)
        is JsonPrimitive -> convertKotlinxPrimitive(element)
    }
}

private fun convertKotlinxObject(kotlinxObject: JsonObject): GsonObject {
    val gsonObject = GsonObject()
    kotlinxObject.forEach { (key, value) ->
        gsonObject.add(key, convertKotlinxToGson(value))
    }
    return gsonObject
}

private fun convertKotlinxArray(kotlinxArray: JsonArray): GsonArray {
    val gsonArray = GsonArray()
    kotlinxArray.forEach { element ->
        gsonArray.add(convertKotlinxToGson(element))
    }
    return gsonArray
}

private fun convertKotlinxPrimitive(primitive: JsonPrimitive): GsonPrimitive {
    return when {
        primitive.isString -> GsonPrimitive(primitive.content)
        else -> when (val content = primitive.content) {
            "true" -> GsonPrimitive(true)
            "false" -> GsonPrimitive(false)
            else -> {
                content.toLongOrNull()?.let { return GsonPrimitive(it) }
                content.toDoubleOrNull()?.let { return GsonPrimitive(it) }

                GsonPrimitive(content)
            }
        }
    }
}