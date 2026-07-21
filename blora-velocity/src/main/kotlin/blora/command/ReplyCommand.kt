package blora.command

import blora.BloraPlugin
import blora.extension.localization
import com.mojang.brigadier.arguments.StringArgumentType
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.audience.send

object ReplyCommand {

    fun register() {
        val commandManager = BloraPlugin.proxyServer.commandManager
        commandManager.register(
            commandManager.metaBuilder("reply")
                .aliases("r")
                .plugin(BloraPlugin.instance)
                .build(),
            BrigadierCommand(
                BrigadierCommand.literalArgumentBuilder("reply")
                    .requires { it.hasPermission("blora.command.tell") }
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString())
                            .executes { context ->
                                if (context.source !is Player) {
                                    context.source.send {
                                        localization {
                                            this.commandErrorMust_be_player
                                        }
                                    }
                                    return@executes 1
                                }
                                val player = context.source as Player
                                val lastUuid = PrivateMessageService.getLastMessager(player)
                                if (lastUuid == null) {
                                    player.send {
                                        localization(player) {
                                            this.commandErrorNo_one_to_reply
                                        }
                                    }
                                    return@executes 1
                                }
                                val optionalTarget = BloraPlugin.proxyServer.getPlayer(lastUuid)
                                if (optionalTarget.isEmpty) {
                                    player.send {
                                        localization(player) {
                                            this.commandErrorReply_target_offline
                                        }
                                    }
                                    return@executes 1
                                }
                                val target = optionalTarget.get()
                                if (player == target) {
                                    player.send {
                                        localization(player) {
                                            this.commandErrorTarget_cannot_be_yourself
                                        }
                                    }
                                    return@executes 1
                                }
                                val message = StringArgumentType.getString(context, "message")
                                PrivateMessageService.send(player, target, message)
                                return@executes 1
                            }
                    )
            )
        )
    }

}
