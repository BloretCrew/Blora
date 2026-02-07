package blora.command

import blora.BloraPlugin
import blora.authorization.BloraAuthorization
import blora.extension.localization
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.audience.send

object LobbyCommand {

    fun register() {
        val commandManager = BloraPlugin.proxyServer.commandManager
        commandManager.register(
            commandManager.metaBuilder("lobby")
                .aliases("hub")
                .plugin(BloraPlugin.instance)
                .build(),
            BrigadierCommand(
                BrigadierCommand.literalArgumentBuilder("lobby")
                    .requires { it.hasPermission("blora.command.lobby") }
                    .executes {
                        if (it.source is Player) {
                            val player = it.source as Player
                            if (!BloraAuthorization.isAuthorized(player))
                                return@executes 1
                            val currentServerOptional = player.currentServer
                            if (currentServerOptional.isPresent) {
                                if (currentServerOptional.get().serverInfo.name == BloraPlugin.limboServer.serverInfo.name)
                                    return@executes 1
                            }
                            player.createConnectionRequest(BloraPlugin.lobbyServer).fireAndForget()
                        } else {
                            it.source.send {
                                localization {
                                    this.commandErrorMust_be_player
                                }
                            }
                        }
                        return@executes 1
                    }
            )
        )
    }

}