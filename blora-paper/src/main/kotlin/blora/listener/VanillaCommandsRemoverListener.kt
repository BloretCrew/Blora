package blora.listener

import blora.command.hook.VanillaCommandHooker
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent

object VanillaCommandsRemoverListener : Listener {

    @EventHandler
    fun onServerLoad(event: ServerLoadEvent) {
        VanillaCommandHooker.hookLoadedStage()
    }

}