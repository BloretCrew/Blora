package blora

import blora.authorization.BloraAuthorization
import blora.command.BloraProxyCommand
import blora.command.OptionsCommand
import blora.configuration.BloraConfiguration
import blora.configuration.ConfigurationContents
import blora.database.BloraDatabase
import blora.listener.BasicListener
import blora.messaging.BloraServer
import blora.protocol.packet.CustomClickAction
import blora.security.PasswordManager
import blora.security.strategy.CharIncludeStrategy
import blora.security.strategy.NoConsecutiveStrategy
import blora.security.strategy.NoDuplicatedStrategy
import blora.security.strategy.NoUsernameStrategy
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.proxy.protocol.ProtocolUtils
import com.velocitypowered.proxy.protocol.StateRegistry
import io.github._4drian3d.vpacketevents.api.register.PacketRegistration
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(
    id = "blora",
    name = "Blora",
    version = "1.0.0",
    authors = ["DeeChael"],
    dependencies = [
        Dependency(id = "luckperms", optional = true),
        Dependency(id = "vpacketevents", optional = false)
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
    private val bloraServer: BloraServer

    init {
        instance = this
        if (!this.init()) {
            throw RuntimeException("Failed to init BloraLogin")
        }

        this.bloraServer = BloraServer(BloraPlugin.configuration.messageing.port)

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
        this.registerPackets()
        this.registerCommands()

        this.initPasswordStrategies()

        this.startServer()

        // this.server.eventManager.register(this, UnauthorizedListener)
        this.server.eventManager.register(this, BasicListener)
    }

    @Subscribe
    fun onShutdown(event: ProxyShutdownEvent) {
        BloraAuthorization.clearAll()
    }

    private fun startServer() {
        this.bloraServer.start()
    }

    private fun registerCommands() {
        OptionsCommand.register()
        BloraProxyCommand.register()
    }

    private fun registerPackets() {
        PacketRegistration.of(CustomClickAction::class.java)
            .direction(ProtocolUtils.Direction.SERVERBOUND)
            .packetSupplier {
                CustomClickAction()
            }
            .stateRegistry(StateRegistry.PLAY)
            .mapping(0x41, ProtocolVersion.MINECRAFT_1_21_6, false)
            .register()
    }

    private fun initPasswordStrategies() {
        PasswordManager.registerStrategy("noUsername", NoUsernameStrategy)
        PasswordManager.registerStrategy("noDuplicated", NoDuplicatedStrategy)
        PasswordManager.registerStrategy("noConsecutive", NoConsecutiveStrategy)
        PasswordManager.registerStrategy("charInclude", CharIncludeStrategy)
    }

    companion object {

        lateinit var instance: BloraPlugin
            private set

        val log: Logger
            get() = instance.logger

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

        val server: BloraServer
            get() = instance.bloraServer

    }

}