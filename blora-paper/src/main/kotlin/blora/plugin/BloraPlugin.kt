package blora.plugin

import blora.chat.PlayerInventoryView
import blora.command.BloraCommandLibWrapper
import blora.command.defaults.BloraCommand
import blora.command.defaults.GuildCommand
import blora.command.defaults.LeadCommand
import blora.command.defaults.MailCommand
import blora.command.defaults.RedeemCommand
import blora.command.defaults.TownCommand
import blora.command.hook.VanillaCommandHooker
import blora.configuration.BloraConfiguration
import blora.configuration.ConfigurationContents
import blora.configuration.GuildVitalityShopConfiguration
import blora.database.BloraDatabase
import blora.entity.QuickEntityLibWrapper
import blora.guild.GuildVitalityManager
import blora.injector.BloraInjector
import blora.internal.api.QuickEntityLib
import blora.internal.api.scheduler.BukkitMain
import blora.listener.*
import blora.localization.BloraLocalization
import blora.menu.MenuApi
import blora.messaging.BloraClient
import blora.permission.Permissions
import blora.scheduler.QuickSchedulerLibWrapper
import blora.scheduler.ShutdownHook
import dev.inmo.krontab.KronScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    val scheduler = KronScheduler
    val scope = CoroutineScope(Dispatchers.BukkitMain)

    private val shutdownHooks = mutableListOf<ShutdownHook>()

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
        GuildVitalityManager.startJob()
        BloraInjector.init()

        connectDatabase()

        initLocalizations()

        startClient()

        Permissions.registerPermissions()
        MenuApi.init()

        registerCommands()
        registerListeners()

        GuildVitalityShopConfiguration.loadOrCreate()
    }

    override fun onDisable() {
        this.shutdownHooks.forEach { it.hook() }
        PlayerInventoryView.stopJob()
        GuildVitalityManager.stopJob()
        BloraInjector.close()
        this.database.disconnect()
        this.client.close()
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

    fun hookShutdown(hook: ShutdownHook) {
        this.shutdownHooks.add(hook)
    }

    fun removeShutdownHook(hook: ShutdownHook) {
        this.shutdownHooks.remove(hook)
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

internal fun registerCommands() {
    BloraCommand.register()
    MailCommand.register()
    RedeemCommand.register()
    GuildCommand.register()
    TownCommand.register()
    LeadCommand.register()
}

internal fun registerListeners() {
    if (BloraPlugin.configuration.security.ensureAuthorized) {
        UnauthorizedListener.register()
    }
    BasicListener.register()
    ChatListener.register()
    GuildListener.register()
    TownListener.register()
    SystemMailListener.register()
    VanillaCommandsRemoverListener.register()
}