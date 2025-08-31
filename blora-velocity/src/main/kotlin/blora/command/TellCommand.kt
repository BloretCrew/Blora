package blora.command

import blora.BloraPlugin
import blora.extension.localization
import com.mojang.brigadier.arguments.StringArgumentType
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.space
import plutoproject.adventurekt.text.text

object TellCommand {

    fun register() {
        val commandManager = BloraPlugin.proxyServer.commandManager
        commandManager.register(
            commandManager.metaBuilder("tell")
                .aliases(
                    "w",
                    "whisper",
                    "msg"
                )
                .plugin(BloraPlugin.instance)
                .build(),
            BrigadierCommand(
                BrigadierCommand.literalArgumentBuilder("tell")
                    .requires { it.hasPermission("blora.command.tell") }
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                            .suggests { context, builder ->
                                val filter: (Player) -> Boolean = if (context.source is Player) {
                                    { player ->
                                        player != context.source
                                    }
                                } else {
                                    { true }
                                }
                                BloraPlugin.proxyServer.allPlayers.filter(filter).map { it.username }
                                    .forEach(builder::suggest)
                                builder.buildFuture()
                            }
                            .then(
                                BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString())
                                    .executes {
                                        if (it.source !is Player) {
                                            it.source.send {
                                                localization {
                                                    this.commandErrorMust_be_player
                                                }
                                            }
                                            return@executes 1
                                        }
                                        val player = it.source as Player
                                        val optionalPlayer = BloraPlugin.proxyServer.getPlayer(
                                            StringArgumentType.getString(
                                                it,
                                                "player"
                                            )
                                        )
                                        if (optionalPlayer.isEmpty) {
                                            player.send {
                                                localization(player) {
                                                    this.commandErrorPlayer_not_exists
                                                }
                                            }
                                            return@executes 1
                                        }
                                        val target = optionalPlayer.get()
                                        if (player == target) {
                                            player.send {
                                                localization(player) {
                                                    this.commandErrorTarget_cannot_be_yourself
                                                }
                                            }
                                            return@executes 1
                                        }
                                        val message = StringArgumentType.getString(it, "message")
                                        player.send {
                                            mini(
                                                BloraPlugin.configuration.chat.privateMessageSendFormat
                                                    .replace("<sender>", player.username)
                                                    .replace("<receiver>", target.username)
                                            ) {
                                                parsedPlaceholder("message", message)
                                            }
                                        }
                                        target.send {
                                            mini(
                                                BloraPlugin.configuration.chat.privateMessageReceiveFormat
                                                    .replace("<sender>", player.username)
                                                    .replace("<receiver>", target.username)
                                            ) {
                                                parsedPlaceholder("message", message)
                                            }
                                        }
                                        val text = component {
                                            text {
                                                player.username
                                            }
                                            space()
                                            text {
                                                "向玩家"
                                            }
                                            space()
                                            text {
                                                target.username
                                            }
                                            space()
                                            text {
                                                "私聊："
                                            }
                                            text {
                                                message
                                            }
                                        }
                                        BloraPlugin.proxyServer.allPlayers
                                            .filter { player -> player.hasPermission("blora.admin.sneakytell") }
                                            .forEach { player ->
                                                player.sendMessage(text)
                                            }
                                        BloraPlugin.proxyServer.consoleCommandSource.sendMessage(text)
                                        return@executes 1
                                    }
                            )
                    )
            )
        )
    }

}