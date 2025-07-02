package net.deechael.blora

import com.google.inject.Inject
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import net.deechael.blora.authorization.BloraAuthorization
import net.deechael.blora.config.BloraConfiguration
import net.deechael.blora.config.ConfigurationContents
import net.deechael.blora.database.BloraDatabase
import net.deechael.blora.dialog.asPacket
import net.deechael.blora.dialog.builtin.eulaDialog
import net.deechael.blora.listener.BasicListener
import net.deechael.blora.security.PasswordManager
import net.deechael.blora.security.strategy.CharIncludeStrategy
import net.deechael.blora.security.strategy.NoConsecutiveStrategy
import net.deechael.blora.security.strategy.NoDuplicatedStrategy
import net.deechael.blora.security.strategy.NoUsernameStrategy
import org.slf4j.Logger
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.text
import java.nio.file.Path

@Plugin(
    id = "blora",
    name = "Blora",
    version = "1.0.0",
    authors = ["DeeChael"],
    dependencies = [
        Dependency(id = "luckperms", optional = true),
        // Dependency(id = "packetevents", optional = false)
    ]
)
class BloraPlugin @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    @DataDirectory private val dataDirectory: Path
) {

    private val configuration: BloraConfiguration = BloraConfiguration(dataDirectory)
    private val database: BloraDatabase
    private val limboServer: RegisteredServer
    private val lobbyServer: RegisteredServer

    init {
        instance = this
        if (!this.init()) {
            throw RuntimeException("Failed to init BloraLogin")
        }

        val limbo = this.server.getServer(this.configuration.contents!!.server.limbo)
        val lobby = this.server.getServer(this.configuration.contents!!.server.lobby)

        if (!limbo.isPresent) {
            this.logger.error("Limbo server not found! Plugin will not be activated!")
            throw RuntimeException("Failed to init BloraLogin")
        }
        if (!lobby.isPresent) {
            this.logger.error("Lobby server not found! Plugin will not be activated!")
            throw RuntimeException("Failed to init BloraLogin")
        }

        this.limboServer = limbo.get()
        this.lobbyServer = lobby.get()

        this.database = BloraDatabase(this.configuration.contents!!.database.buildDataSource())
        this.database.initTables()
    }

    private fun init(): Boolean {
        this.configuration.load()
        if (!this.configuration.contents!!.database.verify()) {
            this.logger.error("Database verification failed! Plugin will not be activated!")
            return false
        }
        if (this.configuration.contents!!.authorization.minUsernameLength > this.configuration.contents!!.authorization.maxUsernameLength) {
            this.logger.error("Max username length is lower than min username length in configuration! Plugin will not be activated!")
            return false
        }
        if (this.configuration.contents!!.security.minPasswordLength > this.configuration.contents!!.security.maxPasswordLength) {
            this.logger.error("Max password length is lower than min password length in configuration! Plugin will not be activated!")
            return false
        }

        return true
    }

    @Subscribe
    fun onInitialize(event: ProxyInitializeEvent) {
        this.initPasswordStrategies()

        // this.server.eventManager.register(this, UnauthorizedListener)
        this.server.eventManager.register(this, BasicListener)

        this.server.commandManager.register(
            this.server.commandManager
                .metaBuilder("blora_velocity_test")
                .plugin(this)
                .build(),
            BrigadierCommand(
                BrigadierCommand.literalArgumentBuilder("blora_velocity_test")
                    .requires { true }
                    .executes {
                        println(it.source)
                        println(it.source.javaClass)
                        if (it.source is Player) {
                            it.source.send {
                                text { "wo cao ni ma!" }
                            }
                            (it.source as ConnectedPlayer).connection.channel.writeAndFlush(eulaDialog().asPacket())
                            //PacketEvents.getAPI().playerManager.sendPacket(it.source, eulaDialog().asPacketEventsPacket())
                        }
                        return@executes 1
                    }
            )
        )
    }

    @Subscribe
    fun onShutdown(event: ProxyShutdownEvent) {
        BloraAuthorization.clearAll()
    }

    private fun initPasswordStrategies() {
        PasswordManager.registerStrategy("noUsername", NoUsernameStrategy)
        PasswordManager.registerStrategy("noDuplicated", NoDuplicatedStrategy)
        PasswordManager.registerStrategy("noConsecutive", NoConsecutiveStrategy)
        PasswordManager.registerStrategy("charInclude", CharIncludeStrategy)
    }

    companion object {

        private lateinit var instance: BloraPlugin

        val proxyServer: ProxyServer
            get() = instance.server

        val dataDirectory: Path
            get() = instance.dataDirectory

        val configuration: ConfigurationContents
            get() = instance.configuration.contents!!

        val database: BloraDatabase
            get() = instance.database

        val limboServer: RegisteredServer
            get() = instance.limboServer

        val lobbyServer: RegisteredServer
            get() = instance.lobbyServer

        val dataFolder: Path
            get() = instance.dataDirectory

    }

}