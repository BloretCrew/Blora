package blora.command

import blora.BloraPlugin
import blora.extension.localization
import com.mojang.brigadier.arguments.StringArgumentType
import com.velocitypowered.api.command.BrigadierCommand
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder

object ResetPasswordCommand {

    private const val PERMISSION = "blora.admin"
    private const val UNREGISTERED = "%unregistered%"

    fun register() {
        val commandManager = BloraPlugin.proxyServer.commandManager
        commandManager.register(
            commandManager.metaBuilder("resetpassword")
                .aliases("resetpw", "rpw")
                .plugin(BloraPlugin.instance)
                .build(),
            BrigadierCommand(
                BrigadierCommand.literalArgumentBuilder("resetpassword")
                    .requires { it.hasPermission(PERMISSION) }
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                            .executes { context ->
                                val source = context.source
                                val username = StringArgumentType.getString(context, "player")

                                // Must be offline on the proxy.
                                if (BloraPlugin.proxyServer.getPlayer(username).isPresent) {
                                    source.send {
                                        localization {
                                            this.commandErrorResetpassword_player_online
                                        }
                                    }
                                    return@executes 1
                                }

                                val databasePlayer = BloraPlugin.database.getPlayerByName(username)
                                if (databasePlayer == null) {
                                    source.send {
                                        localization {
                                            this.commandErrorPlayer_not_exists
                                        }
                                    }
                                    return@executes 1
                                }

                                if (databasePlayer.hashedPassword1 == UNREGISTERED) {
                                    source.send {
                                        localization {
                                            this.commandErrorResetpassword_not_registered
                                        }
                                    }
                                    return@executes 1
                                }

                                BloraPlugin.database.trans {
                                    databasePlayer.hashedPassword1 = UNREGISTERED
                                    databasePlayer.hashedPassword2 = UNREGISTERED
                                    databasePlayer.hashedPassword3 = UNREGISTERED
                                    databasePlayer.flush()
                                }

                                source.send {
                                    localization(
                                        tags = {
                                            parsedPlaceholder("player", databasePlayer.username)
                                        }
                                    ) {
                                        this.commandSuccessResetpassword
                                    }
                                }
                                BloraPlugin.log.info(
                                    "[ADMIN] ${source} reset password for player ${databasePlayer.username}"
                                )
                                return@executes 1
                            }
                    )
            )
        )
    }

}
