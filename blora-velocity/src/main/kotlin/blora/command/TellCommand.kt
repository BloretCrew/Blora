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
                                // Filter by the characters already typed (builder.remaining),
                                // otherwise the client always sees the full default-order player list.
                                val prefix = builder.remaining.lowercase()
                                val source = context.source
                                BloraPlugin.proxyServer.allPlayers
                                    .asSequence()
                                    .filter { player ->
                                        if (source is Player) player != source else true
                                    }
                                    .map { it.username }
                                    .filter { name ->
                                        prefix.isEmpty() || name.lowercase().startsWith(prefix)
                                    }
                                    .sortedBy { it.lowercase() }
                                    .forEach { name ->
                                        builder.suggest(name)
                                    }
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
                                        val chatConfig = BloraPlugin.configuration.chat
                                        player.send {
                                            mini(
                                                chatConfig.privateMessageSendFormat
                                                    .replace("<sender>", player.username)
                                                    .replace("<receiver>", target.username)
                                            ) {
                                                parsedPlaceholder("message", message)
                                            }
                                        }
                                        target.send {
                                            mini(
                                                chatConfig.privateMessageReceiveFormat
                                                    .replace("<sender>", player.username)
                                                    .replace("<receiver>", target.username)
                                            ) {
                                                parsedPlaceholder("message", message)
                                            }
                                        }
                                        val spyText = component {
                                            mini(
                                                chatConfig.privateMessageSpyFormat
                                                    .replace("<sender>", player.username)
                                                    .replace("<receiver>", target.username)
                                            ) {
                                                parsedPlaceholder("message", message)
                                            }
                                        }
                                        BloraPlugin.proxyServer.allPlayers
                                            .filter { online ->
                                                online != player &&
                                                    online != target &&
                                                    online.hasPermission(SpyCommand.PERMISSION) &&
                                                    PrivateMessageSpy.isEnabled(online)
                                            }
                                            .forEach { online ->
                                                online.sendMessage(spyText)
                                            }
                                        // Console always receives spy copies for audit.
                                        BloraPlugin.proxyServer.consoleCommandSource.sendMessage(spyText)
                                        return@executes 1
                                    }
                            )
                    )
            )
        )
    }

}