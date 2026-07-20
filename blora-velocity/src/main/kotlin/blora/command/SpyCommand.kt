package blora.command

import blora.BloraPlugin
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.mini

object SpyCommand {

    const val PERMISSION = "blora.admin.sneakytell"

    fun register() {
        val commandManager = BloraPlugin.proxyServer.commandManager
        // requires(permission): no tab completion / listing for players without permission.
        // Players who somehow invoke it still get the same "unknown command" style message.
        commandManager.register(
            commandManager.metaBuilder("spy")
                .plugin(BloraPlugin.instance)
                .build(),
            BrigadierCommand(
                BrigadierCommand.literalArgumentBuilder("spy")
                    .requires { source -> source is Player && source.hasPermission(PERMISSION) }
                    .executes { context ->
                        val source = context.source
                        if (source !is Player || !source.hasPermission(PERMISSION)) {
                            sendUnknownCommand(source, "spy")
                            return@executes 0
                        }
                        val enabled = PrivateMessageSpy.toggle(source)
                        val chatConfig = BloraPlugin.configuration.chat
                        source.send {
                            mini(
                                if (enabled) {
                                    chatConfig.privateMessageSpyEnabledMessage
                                } else {
                                    chatConfig.privateMessageSpyDisabledMessage
                                }
                            )
                        }
                        return@executes 1
                    }
            )
        )
    }

    private fun sendUnknownCommand(source: CommandSource, label: String) {
        // Same key Velocity uses for unknown commands — looks like a non-existent command.
        source.sendMessage(
            Component.translatable(
                "velocity.command.command-does-not-exist",
                NamedTextColor.RED,
                Component.text(label)
            )
        )
    }

}
