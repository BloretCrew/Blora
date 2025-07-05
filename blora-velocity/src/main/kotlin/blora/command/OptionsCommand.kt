package blora.command

import blora.BloraPlugin
import blora.dialog.asPacket
import blora.dialog.builtin.optionsDialog
import blora.extension.sendPacket
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.style.red
import plutoproject.adventurekt.text.style.text
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with

object OptionsCommand {

    fun register() {
        val commandManager = BloraPlugin.proxyServer.commandManager
        commandManager.register(
            commandManager.metaBuilder("options")
                .plugin(BloraPlugin.instance)
                .build(),
            BrigadierCommand(
                BrigadierCommand.literalArgumentBuilder("options")
                    .requires { it.hasPermission("blora.command.options") }
                    .executes {
                        if (it.source is Player) {
                            (it.source as Player).sendPacket(optionsDialog().asPacket())
                        } else {
                            it.source.send {
                                text { "只有玩家可以运行这个命令" } with red.text
                            }
                        }
                        return@executes 1
                    }
            )
        )
    }

}