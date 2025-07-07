package blora.plugin

import blora.api.QuickEntityLib
import blora.command.QuickCommandLibWrapper
import blora.command.defaults.BloraCommand
import blora.configuration.BloraConfiguration
import blora.configuration.ConfigurationContents
import blora.database.BloraDatabase
import blora.entity.QuickEntityLibWrapper
import blora.listener.UnauthorizedListener
import blora.messaging.BloraClient
import blora.modules.ModuleManager
import blora.modules.mail.MailModule
import blora.scheduler.QuickSchedulerLibWrapper
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.net.InetAddress

object BloraPlugin : JavaPlugin(), blora.api.QuickLib {

    lateinit var client: BloraClient
    internal lateinit var configurationLoader: BloraConfiguration
    internal lateinit var databaseLoader: BloraDatabase
    val configuration: ConfigurationContents
        get() = configurationLoader.contents!!
    val database: BloraDatabase
        get() = this.databaseLoader


    override fun onEnable() {
        initFolders()
        loadConfiguration()
        if (!this.configuration.database.verify()) {
            this.slF4JLogger.error("Database verification failed! Plugin will not be activated!")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        startClient()

        registerModules()
        registerCommands()
        registerListeners()

        ModuleManager.enable()
    }

    override fun onDisable() {
        this.client.close()
        ModuleManager.disable()
    }

    override fun getCommandLib(): blora.api.command.QuickCommandLib {
        return QuickCommandLibWrapper
    }

    override fun getSchedulerLib(): blora.api.scheduler.QuickSchedulerLib {
        return QuickSchedulerLibWrapper
    }

    override fun getEntityLib(): QuickEntityLib {
        return QuickEntityLibWrapper
    }

    fun reloadConfiguration() {
        this.configurationLoader.load()
        if (this.configuration.security.ensureAuthorized) {
            UnauthorizedListener.register()
        } else {
            UnauthorizedListener.unregister()
        }
    }

}

internal fun initFolders() {
    if (!BloraPlugin.dataFolder.exists()) {
        BloraPlugin.dataFolder.mkdirs()
    }
}

internal fun loadConfiguration() {
    BloraPlugin.configurationLoader = BloraConfiguration(BloraPlugin.dataPath)
    BloraPlugin.configurationLoader.load()
}

internal fun connectDatabase() {
    BloraPlugin.databaseLoader = BloraDatabase(BloraPlugin.configuration.database.buildDataSource())
    BloraPlugin.database.initTables()
}

internal fun startClient() {
    BloraPlugin.client = BloraClient(
        InetAddress.getByName(BloraPlugin.configuration.messageing.host),
        BloraPlugin.configuration.messageing.port
    )
}

internal fun registerModules() {
    ModuleManager.registerModule(MailModule)
}

internal fun registerCommands() {
    BloraCommand.register()
}

internal fun registerListeners() {
    if (BloraPlugin.configuration.security.ensureAuthorized) {
        UnauthorizedListener.register()
    }
}