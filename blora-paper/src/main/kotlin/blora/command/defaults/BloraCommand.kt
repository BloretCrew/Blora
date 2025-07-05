package blora.command.defaults

import blora.api.command.*
import blora.api.command.argument.Arguments
import blora.api.message.*
import blora.messaging.packet.common.DebugMessagePacket
import blora.modules.ModuleManager
import blora.plugin.BloraPlugin
import plutoproject.adventurekt.text.*
import plutoproject.adventurekt.text.style.gray
import plutoproject.adventurekt.text.style.showText
import plutoproject.adventurekt.text.style.text

object BloraCommand {

    fun register() {
        QuickCommandLib.registerCommand("blora") {
            meta {
                alias("bloret")
                alias("blg")
                alias("bailuogu")
            }
            requires {
                return@requires this.asBukkit.hasPermission("bloret.basic.command.blora")
            }
            // permission("bloret.basic.command.blora") 这一条等于上面的 requires 块，requires 和 permission 函数都是后优先函数

            literal("modules") {
                executor {
                    this.invoker.sendMessage {
                        newline()
                        managementPrefix()
                        primaryMessage("当前正在运行的模块：")
                        for (module in ModuleManager.listModules()) {
                            newline()
                            managementPrefix()
                            text("- ") with gray.text
                            text(module.name) with showText {
                                raw(module.displayName)
                                newline()
                                text("状态：") with gray.text
                                if (module.enabled) {
                                    text("已启用") with QuickColors.success.text
                                } else {
                                    text("未启用") with QuickColors.error.text
                                }
                            }
                        }
                    }
                }
            }

            literal("proxy") {
                literal("debug") {
                    argument("message", Arguments.greedyString) { messageFunc ->
                        executor {
                            val message = messageFunc()
                            BloraPlugin.client.send(DebugMessagePacket().apply { this.message = message })
                        }
                    }
                }
            }

            literal("debug") {
                requires {
                    // 只有 Rhedar 和 DeeChael 能使用调试用途的命令哦
                    return@requires (this.asBukkit.name.lowercase().contentEquals("deechael") ||
                            this.asBukkit.name.lowercase().contentEquals("rhedar") || this.isConsole)
                }

                literal("executor") {
                    literal("normal") {
                        executor {
                            this.invoker.sendMessage {
                                commandPrefix()
                                primaryMessage("所有命令执行者都可以执行的命令")
                            }
                        }
                    }
                    literal("player") {
                        playerExecutor {
                            this.invoker.sendMessage {
                                commandPrefix()
                                primaryMessage("仅有玩家执行者可以执行的命令")
                            }
                        }
                    }
                    literal("block") {
                        blockExecutor {
                            this.invoker.sendMessage {
                                commandPrefix()
                                primaryMessage("仅有方块执行者可以执行的命令")
                            }
                        }
                    }
                }

                literal("command") {
                    literal("literal") {
                        playerExecutor {
                            this.invoker.sendMessage {
                                commandPrefix()
                                primaryMessage("您正在作为一个玩家执行调试命令")
                            }
                        }
                        executor {
                            this.invoker.sendMessage {
                                commandPrefix()
                                primaryMessage("您正在作为一个非玩家命令执行者执行调试命令")
                            }
                        }
                    }

                    literal("argument") {
                        literal("string") {
                            literal("string") {
                                argument("value", Arguments.string) { getValue ->
                                    executor {
                                        val value = getValue()
                                        this.invoker.sendMessage {
                                            newline()
                                            commandPrefix()
                                            primaryMessage("您正在调试命令参数")
                                            space()
                                            primaryHighlightMessage("字符串")
                                            newline()
                                            primaryMessage("您输入的值为")
                                            space()
                                            primaryHighlightMessage(value)
                                        }
                                    }
                                }
                            }
                            literal("word") {
                                argument("value", Arguments.word) { getValue ->
                                    executor {
                                        val value = getValue()
                                        this.invoker.sendMessage {
                                            newline()
                                            commandPrefix()
                                            primaryMessage("您正在调试命令参数")
                                            space()
                                            primaryHighlightMessage("字符串-单词")
                                            newline()
                                            primaryMessage("您输入的值为")
                                            space()
                                            primaryHighlightMessage(value)
                                        }
                                    }
                                }
                            }
                            literal("greedy") {
                                argument("value", Arguments.greedyString) { getValue ->
                                    executor {
                                        val value = getValue()
                                        this.invoker.sendMessage {
                                            newline()
                                            commandPrefix()
                                            primaryMessage("您正在调试命令参数")
                                            space()
                                            primaryHighlightMessage("字符串-贪婪")
                                            newline()
                                            primaryMessage("您输入的值为")
                                            space()
                                            primaryHighlightMessage(value)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}