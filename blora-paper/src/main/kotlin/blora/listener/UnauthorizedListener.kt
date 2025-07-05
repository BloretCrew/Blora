package blora.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object UnauthorizedListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {

    }

}