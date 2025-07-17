package blora.plugin

import blora.internal.api.QuickEntityLib
import blora.chat.PlayerInventoryView
import blora.command.BloraCommandLibWrapper
import blora.command.defaults.BloraCommand
import blora.command.defaults.MailCommand
import blora.command.defaults.RedeemCommand
import blora.command.hook.VanillaCommandHooker
import blora.configuration.BloraConfiguration
import blora.configuration.ConfigurationContents
import blora.database.BloraDatabase
import blora.entity.QuickEntityLibWrapper
import blora.injector.BloraInjector
import blora.listener.ChatListener
import blora.listener.SystemMailListener
import blora.listener.UnauthorizedListener
import blora.listener.VanillaCommandsRemoverListener
import blora.localization.BloraLocalization
import blora.menu.MenuApi
import blora.messaging.BloraClient
import blora.modules.ModuleManager
import blora.modules.mail.MailModule
import blora.permission.Permissions
import blora.scheduler.QuickSchedulerLibWrapper
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.InetAddress

object BloraPlugin : JavaPlugin(), blora.internal.api.QuickLib {

    lateinit var client: BloraClient
    internal lateinit var configurationLoader: BloraConfiguration
    internal lateinit var databaseLoader: BloraDatabase
    val configuration: ConfigurationContents
        get() = configurationLoader.contents!!
    val database: BloraDatabase
        get() = this.databaseLoader
    val localeDirectory: File
        get() = File(this.dataFolder, "locales")


    override fun onEnable() {
        ThirdPartys.init()

        initFolders()
        loadConfiguration()
        if (!this.configuration.database.verify()) {
            this.slF4JLogger.error("Database verification failed! Plugin will not be activated!")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        VanillaCommandHooker.hookEnableStage()
        PlayerInventoryView.startJob()
        BloraInjector.init()

        connectDatabase()

        initLocalizations()

        startClient()

        Permissions.registerPermissions()
        MenuApi.init()

        registerModules()
        registerCommands()
        registerListeners()

        ModuleManager.enable()
    }

    override fun onDisable() {
        PlayerInventoryView.stopJob()
        BloraInjector.close()
        this.client.close()
        ModuleManager.disable()
    }

    override fun getCommandLib(): blora.internal.api.command.BloraCommandLib {
        return BloraCommandLibWrapper
    }

    override fun getSchedulerLib(): blora.internal.api.scheduler.QuickSchedulerLib {
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
    if (!BloraPlugin.localeDirectory.exists()) {
        BloraPlugin.localeDirectory.mkdirs()
    }
}

private fun initLocalizations() {
    BloraLocalization.saveDefaultLocalization()
    BloraLocalization.loadLocalizations()
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
        InetAddress.getByName(BloraPlugin.configuration.messaging.host),
        BloraPlugin.configuration.messaging.port
    )
}

internal fun registerModules() {
    ModuleManager.registerModule(MailModule)
}

internal fun registerCommands() {
    BloraCommand.register()
    MailCommand.register()
    RedeemCommand.register()
}

internal fun registerListeners() {
    if (BloraPlugin.configuration.security.ensureAuthorized) {
        UnauthorizedListener.register()
    }
    SystemMailListener.register()
    Bukkit.getPluginManager().registerEvents(VanillaCommandsRemoverListener, BloraPlugin)
    Bukkit.getPluginManager().registerEvents(ChatListener, BloraPlugin)
}