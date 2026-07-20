@file:OptIn(ExperimentalSerializationApi::class)

package blora.localization

import blora.extension.toStringTag
import blora.plugin.BloraPlugin
import blora.serialization.json.PointSplit
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.bukkit.entity.Player
import java.io.File

object BloraLocalization {

    val fallbackLocalization = LocalizationContents()

    private val localizations = mutableMapOf<String, LocalizationContents>()

    val localizationJson = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.PointSplit
        prettyPrint = true
    }

    fun saveDefaultLocalization() {
        this.ensureLocalizationFile(File(BloraPlugin.localeDirectory, "zh_cn.json"))
    }

    fun ensureLocalizationFile(file: File): LocalizationContents {
        if (!file.exists()) {
            file.createNewFile()
            with(file.bufferedWriter(Charsets.UTF_8)) {
                this.write(this@BloraLocalization.localizationJson.encodeToString(LocalizationContents()))
                this.close()
            }
        }
        val contents = with(file.bufferedReader(Charsets.UTF_8)) {
            return@with this@BloraLocalization.localizationJson.decodeFromString<LocalizationContents>(this.readText())
        }
        with(file.bufferedWriter(Charsets.UTF_8)) {
            this.write(this@BloraLocalization.localizationJson.encodeToString(contents))
            this.close()
        }
        return contents
    }

    fun loadLocalizations() {
        localizations.clear()
        val files = BloraPlugin.localeDirectory.listFiles() ?: return
        for (file in files) {
            if (file.isFile) {
                try {
                    val localization = this.ensureLocalizationFile(file)
                    localizations[file.nameWithoutExtension.lowercase()] = localization
                } catch (ex: Exception) {
                    continue
                }
            }
        }
    }

    fun getLocalization(player: Player): LocalizationContents {
        return this.localizations[player.locale().toStringTag().lowercase()] ?: this.fallbackLocalization
    }

}