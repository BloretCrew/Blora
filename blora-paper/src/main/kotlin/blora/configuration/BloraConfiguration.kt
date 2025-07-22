package blora.configuration

import blora.plugin.BloraPlugin
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.peanuuutz.tomlkt.Toml
import java.io.File
import java.nio.file.Path

class BloraConfiguration(
    baseDirectory: Path
) {

    private val toml: Toml = Toml {
        ignoreUnknownKeys = true
    }

    private val configurationFile = File(baseDirectory.toFile(), "config.toml")

    var contents: ConfigurationContents? = null

    init {
        if (!baseDirectory.toFile().isDirectory()) {
            baseDirectory.toFile().mkdirs()
        }
    }

    fun init() {
        if (!configurationFile.exists()) {
            configurationFile.createNewFile()
            with(configurationFile.bufferedWriter(Charsets.UTF_8)) {
                this.write(this@BloraConfiguration.toml.encodeToString(ConfigurationContents()))
                this.close()
            }
        }
    }

    fun load() {
        this.init()
        this.contents = with(configurationFile.bufferedReader(Charsets.UTF_8)) {
            return@with this@BloraConfiguration.toml.decodeFromString<ConfigurationContents>(this.readText())
        }
        this.save()
    }

    fun save() {
        if (this.contents != null) {
            with(configurationFile.bufferedWriter(Charsets.UTF_8)) {
                this.write(this@BloraConfiguration.toml.encodeToString(this@BloraConfiguration.contents))
                this.close()
            }
        }
    }

}

val CONF: ConfigurationContents
    get() = BloraPlugin.configuration