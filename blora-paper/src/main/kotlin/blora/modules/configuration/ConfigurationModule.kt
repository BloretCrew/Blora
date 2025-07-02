@file:OptIn(ExperimentalSerializationApi::class)

package blora.modules.configuration

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.style.rgb
import plutoproject.adventurekt.text.style.text
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with
import blora.modules.Module
import blora.plugin.BloraPlugin
import java.io.File

object ConfigurationModule : Module {

    override val id: String = "configuration"
    override val name: String = "Configuration"
    override val displayName: Component = component {
        text("配置文件模块") with rgb(137, 129, 124).text
    }
    override var enabled: Boolean = false
    override val dependencies: List<String> = listOf()

    val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
        coerceInputValues = true

        namingStrategy = JsonNamingStrategy.SnakeCase

        // json5 options
        isLenient = true
        allowTrailingComma = true
        allowComments = true
    }

    fun saveConfiguration(name: String, data: Any, parentDir: File? = null) {
        // TODO: configuration file type
        val directoryToSave = parentDir ?: File(BloraPlugin.dataFolder, "$name.json")
        this.json.encodeToString(data)
    }

}