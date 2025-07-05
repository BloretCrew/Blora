package blora.plugin

import blora.api.QuickEntityLib
import blora.command.QuickCommandLibWrapper
import blora.command.defaults.BloraCommand
import blora.configuration.BloraConfiguration
import blora.configuration.ConfigurationContents
import blora.entity.QuickEntityLibWrapper
import blora.messaging.BloraClient
import blora.modules.ModuleManager
import blora.modules.userManagement.UserManagementModule
import blora.scheduler.QuickSchedulerLibWrapper
import org.bukkit.plugin.java.JavaPlugin
import java.net.InetAddress

object BloraPlugin : JavaPlugin(), blora.api.QuickLib {

    lateinit var client: BloraClient
    internal lateinit var configurationLoader: BloraConfiguration
    val configuration: ConfigurationContents
        get() = configurationLoader.contents!!


    override fun onEnable() {
        initFolders()
        loadConfiguration()
        startClient()

        registerModules()
        registerCommands()

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

internal fun startClient() {
    BloraPlugin.client = BloraClient(
        InetAddress.getByName(BloraPlugin.configuration.messageing.host),
        BloraPlugin.configuration.messageing.port
    )
}

internal fun registerModules() {
    ModuleManager.registerModule(UserManagementModule)
}

internal fun registerCommands() {
    BloraCommand.register()
}