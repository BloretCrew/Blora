@file:OptIn(ExperimentalSerializationApi::class)

package blora.options

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

val PLAYER_OPTIONS_JSON = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    namingStrategy = JsonNamingStrategy.SnakeCase
}

@Serializable
data class PlayerOptions(
    var alwaysLobby: OptionStatus = OptionStatus.NOT_SET
)

enum class OptionStatus {

    ENABLE, DISABLE, NOT_SET

}
