package blora.command

import blora.BloraPlugin
import blora.dialog.asPacket
import blora.extension.sendPacket
import blora.mail.mailDialog
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.space
import plutoproject.adventurekt.text.style.green
import plutoproject.adventurekt.text.style.red
import plutoproject.adventurekt.text.style.text
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with

object BloraProxyCommand {

    fun register() {
        val commandManager = BloraPlugin.proxyServer.commandManager
        commandManager.register(
            commandManager.metaBuilder("bloraproxy")
                .plugin(BloraPlugin.instance)
                .build(),
            BrigadierCommand(
                BrigadierCommand.literalArgumentBuilder("bloraproxy")
                    .requires { it.hasPermission("blora.command.bloraproxy") }
                    .then(
                        BrigadierCommand.literalArgumentBuilder("list")
                            .executes {
                                it.source.send {
                                    text { "服务器连接状态：" }
                                    for (server in BloraPlugin.proxyServer.allServers) {
                                        newline()
                                        text { "●" } with if (BloraPlugin.server.isConnected(server.serverInfo.name))
                                            green.text
                                        else
                                            red.text
                                        space()
                                        text { server.serverInfo.name }
                                    }
                                }
                                return@executes 1
                            }
                    )
                    .then(
                        BrigadierCommand.literalArgumentBuilder("debug")
                            .then(
                                BrigadierCommand.literalArgumentBuilder("mailDialog")
                                    .executes {
                                        if (it.source is Player) {
                                            (it.source as Player).sendPacket(mailDialog().asPacket())
                                        }
                                        return@executes 1
                                    }
                            )
                    )
            )
        )
    }

}