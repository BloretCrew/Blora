package blora.listener

import blora.command.hook.VanillaCommandHooker
import blora.plugin.BloraPlugin
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent

object VanillaCommandsRemoverListener : Listener {

    private var registered: Boolean = false

    fun register() {
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
            registered = true
        }
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
        this.registered = false
    }

    @EventHandler
    fun onServerLoad(event: ServerLoadEvent) {
        VanillaCommandHooker.hookLoadedStage()
    }

}