package blora.command.defaults

import blora.internal.api.command.*
import blora.internal.api.command.argument.Arguments
import blora.messaging.packet.common.DebugMessagePacket
import blora.messaging.packet.common.ReloadConfigurationPacket
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import plutoproject.adventurekt.text.text

object BloraCommand {

    fun register() {
        BloraCommandLib.registerCommand("blora") {
            meta {
                alias("bloret")
                alias("blg")
                alias("bailuogu")
            }
            requires {
                return@requires this.asBukkit.hasPermission(Permissions.Commands.Blora)
                        || this.asBukkit.hasPermission(Permissions.Admin)
            }

            literal("proxy") {
                literal("debug") {
                    requires {
                        // 只有 Rhedar 和 DeeChael 能使用调试用途的命令哦
                        return@requires (this.asBukkit.name.lowercase().contentEquals("deechael") ||
                                this.asBukkit.name.lowercase().contentEquals("rhedar") || this.isConsole)
                    }
                    argument("message", Arguments.greedyString) { messageFunc ->
                        executor {
                            val message = messageFunc()
                            BloraPlugin.client.send(DebugMessagePacket().apply { this.message = message })
                        }
                    }
                }
                literal("reconnect") {
                    executor {
                        if (BloraPlugin.client.isConnected()) {
                            this.invoker.sendMessage {
                                text { "已连接，无需重连" }
                            }
                            return@executor
                        }
                        BloraPlugin.client.reconnect()
                        this.invoker.sendMessage {
                            text { "已尝试重连" }
                        }
                    }
                }
            }

            literal("reload") {
                executor {
                    BloraPlugin.client.send(ReloadConfigurationPacket)
                }
            }

            literal("debug") {
                requires {
                    // 只有 Rhedar 和 DeeChael 能使用调试用途的命令哦
                    return@requires (this.asBukkit.name.lowercase().contentEquals("deechael") ||
                            this.asBukkit.name.lowercase().contentEquals("rhedar") || this.isConsole)
                }
            }
        }
    }

}