package blora.command

import blora.BloraPlugin
import blora.authorization.AuthorizationFunctions
import blora.authorization.BloraAuthorization
import blora.extension.localization
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.audience.send

object ChangePasswordCommand {

    fun register() {
        val commandManager = BloraPlugin.proxyServer.commandManager
        commandManager.register(
            commandManager.metaBuilder("changepassword")
                .aliases("changepw", "cpw")
                .plugin(BloraPlugin.instance)
                .build(),
            BrigadierCommand(
                BrigadierCommand.literalArgumentBuilder("changepassword")
                    .requires { it is Player }
                    .executes { context ->
                        val player = context.source as Player
                        if (!BloraAuthorization.isAuthorized(player)) {
                            return@executes 1
                        }
                        val databasePlayer = BloraPlugin.database.getPlayerByName(player.username)
                        if (databasePlayer == null || databasePlayer.hashedPassword1 == "%unregistered%") {
                            player.send {
                                localization(player) {
                                    // Online-mode / premium accounts may never set a password.
                                    if (player.isOnlineMode || databasePlayer?.premiumUuid != null) {
                                        this.commandErrorChangepassword_premium_no_password
                                    } else {
                                        this.commandErrorChangepassword_not_registered
                                    }
                                }
                            }
                            return@executes 1
                        }
                        AuthorizationFunctions.showChangePasswordDialog(player)
                        return@executes 1
                    }
            )
        )
    }

}
