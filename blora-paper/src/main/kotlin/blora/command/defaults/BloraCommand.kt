package blora.command.defaults

import blora.internal.api.command.*
import blora.internal.api.command.argument.Arguments
import blora.extension.localization
import blora.messaging.packet.common.DebugMessagePacket
import blora.messaging.packet.common.ReloadConfigurationPacket
import blora.modules.ModuleManager
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import plutoproject.adventurekt.text.*

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
            // permission("bloret.basic.command.blora") 这一条等于上面的 requires 块，requires 和 permission 函数都是后优先函数

            literal("modules") {
                executor {
                    val player = if (this.invoker.isPlayer)
                        this.invoker.player().asBukkit
                    else
                        null
                    this.invoker.sendMessage {
                        newline()
                        localization(player) {
                            this.commandBloraModulesMessage
                        }
                        for (module in ModuleManager.listModules()) {
                            newline()
                            localization(
                                player = player,
                                tags = {
                                    parsedPlaceholder("module_name", module.name)
                                    componentPlaceholder("module_displayName") {
                                        raw { module.displayName }
                                    }
                                    componentPlaceholder("module_status") {
                                        localization(player) {
                                            if (module.enabled) {
                                                this.commandBloraModulesModuleStatusEnabled
                                            } else {
                                                this.commandBloraModulesModuleStatusDisabled
                                            }
                                        }
                                    }
                                }
                            ) {
                                this.commandBloraModulesModule
                            }
                        }
                    }
                }
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