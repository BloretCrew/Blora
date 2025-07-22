package blora.json

import kotlinx.serialization.json.Json

val STORE_DATA_JSON = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}