package blora.listener

import blora.authorization.BloraAuthorization
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.Player

object UnauthorizedListener {

    @Suppress("DEPRECATION")
    @Subscribe(priority = 1000)
    fun onPlayerChat(event: PlayerChatEvent) {
        if (!BloraAuthorization.isAuthorized(event.player)) {
            event.result = PlayerChatEvent.ChatResult.denied()
        }
    }

    @Subscribe(priority = 1000)
    fun onCommandExecute(event: CommandExecuteEvent) {
        val player = event.commandSource
        if (player !is Player)
            return
        if (BloraAuthorization.isAuthorized(player))
            return

        // theoretically, with dialog was introduced and authorization is moved to dialog
        // player can send no commands anymore

        /*
        val command = event.command.split(" ")[0]
        if (BloraPlugin.configuration.security.unauthorizedAllowedCommands.contains(command))
            return
        */

        event.result = CommandExecuteEvent.CommandResult.denied()
    }

}