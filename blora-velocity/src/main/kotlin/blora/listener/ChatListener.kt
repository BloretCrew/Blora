package blora.listener

import blora.BloraPlugin
import blora.extension.localization
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.space
import plutoproject.adventurekt.text.text

object ChatListener {

    private val PRIVATE_MESSAGE_COMMANDS = listOf(
        "tell",
        "w",
        "whisper",
        "msg",
        "blora:tell",
        "blora:w",
        "blora:whisper",
        "blora:msg"
    )

    @Subscribe
    fun onCommandExecute(event: CommandExecuteEvent) {
        val rawCommand = event.command.trim()
        if (rawCommand.contains(" ")) {
            val split = rawCommand.split(" ")
            val command = split[0]
            if (PRIVATE_MESSAGE_COMMANDS.any { it.contentEquals(command.lowercase(), true) }) {
                event.result = CommandExecuteEvent.CommandResult.forwardToServer()
                if (event.commandSource is Player) {
                    val player = event.commandSource as Player
                    if (!(player.hasPermission("bloret.command.tell") || player.hasPermission("bloret.admin")))
                        return
                    val arguments = split.subList(1, split.size).toTypedArray()
                    if (arguments.size < 2) {
                        player.send {
                            localization(player) {
                                this.commandErrorInvalid
                            }
                        }
                        return
                    }
                    val playerName = arguments[0]
                    val targetOptional = BloraPlugin.proxyServer.getPlayer(playerName)
                    if (targetOptional.isEmpty) {
                        player.send {
                            localization(player) {
                                this.commandErrorPlayer_not_exists
                            }
                        }
                        return
                    }
                    val message = arguments.toList().subList(1, arguments.size).joinToString(" ")
                    val target = targetOptional.get()
                    target.send {
                        text { player.username }
                        space()
                        text { "给你说：" }
                        text { message }
                    }
                }
            }
        } else {
            if (PRIVATE_MESSAGE_COMMANDS.any { it.contentEquals(rawCommand.lowercase(), true) }) {
                event.result = CommandExecuteEvent.CommandResult.forwardToServer()
                if (event.commandSource is Player) {
                    val player = event.commandSource as Player
                    if (!(player.hasPermission("bloret.command.tell") || player.hasPermission("bloret.admin")))
                        return
                    event.commandSource.send {
                        localization(event.commandSource as Player) {
                            this.commandErrorInvalid
                        }
                    }
                }
            }
        }
    }

}