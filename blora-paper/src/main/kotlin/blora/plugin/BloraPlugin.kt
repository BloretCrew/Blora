package blora.plugin

import org.bukkit.plugin.java.JavaPlugin
import blora.api.QuickEntityLib
import blora.command.QuickCommandLibWrapper
import blora.command.defaults.BloraCommand
import blora.debug.DialogDebuger
import blora.entity.QuickEntityLibWrapper
import blora.modules.ModuleManager
import blora.modules.userManagement.UserManagementModule
import blora.scheduler.QuickSchedulerLibWrapper

object BloraPlugin : JavaPlugin(), blora.api.QuickLib {

    override fun onEnable() {
        initFolders()

        registerModules()
        registerCommands()

        ModuleManager.enable()

        DialogDebuger.debug()
    }

    override fun onDisable() {
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

internal fun registerModules() {
    ModuleManager.registerModule(UserManagementModule)
}

internal fun registerCommands() {
    BloraCommand.register()
}