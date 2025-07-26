package blora.command.defaults

import blora.database.DB
import blora.extension.format
import blora.guild.menu.guildMenu
import blora.internal.api.command.BloraCommandLib
import blora.internal.api.command.argument
import blora.internal.api.command.argument.Arguments
import blora.internal.api.command.executor
import blora.internal.api.command.literal
import blora.internal.api.command.playerExecutor
import blora.internal.api.command.requires
import blora.internal.api.command.suggests
import blora.permission.Permissions
import com.mojang.brigadier.arguments.StringArgumentType
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.space
import plutoproject.adventurekt.text.text
import kotlin.math.max

object GuildCommand {

    fun register() {
        BloraCommandLib.registerCommand("guild") {
            requires {
                this.hasPermission(Permissions.Commands.Guild)
            }

            playerExecutor {
                guildMenu(this.player.asBukkit).open()
            }

            literal("admin") {
                requires {
                    this.hasPermission(Permissions.Admin) || this.isConsole
                }

                argument("guild", Arguments.word) { getGuildId ->
                    suggests {
                        DB.listGuilds().forEach {
                            this.suggest(it.gid)
                        }
                    }
                    literal("vitality") {
                        literal("add") {
                            executor {
                                val guild = DB.getGuildByGid(getGuildId())
                                if (guild == null) {
                                    this.invoker.sendMessage {
                                        text { "公会不存在" }
                                    }
                                    return@executor
                                }
                                DB.trans {
                                    guild.refresh()
                                }
                                this.invoker.sendMessage {
                                    text { "公会" }
                                    space()
                                    mini(guild.displayName)
                                    space()
                                    text { "目前拥有" }
                                    space()
                                    text { guild.vitality.format(2) }
                                    space()
                                    text { "活跃点" }
                                }
                            }
                        }
                        literal("add") {
                            argument("value", Arguments.double(0.0)) { getValue ->
                                executor {
                                    val guild = DB.getGuildByGid(getGuildId())
                                    if (guild == null) {
                                        this.invoker.sendMessage {
                                            text { "公会不存在" }
                                        }
                                        return@executor
                                    }
                                    val value = getValue()
                                    DB.trans {
                                        guild.refresh()
                                        guild.vitality += value
                                        guild.flush()
                                    }
                                    this.invoker.sendMessage {
                                        text { "成功为公会" }
                                        space()
                                        mini(guild.displayName)
                                        space()
                                        text { "添加了" }
                                        space()
                                        text { value.format(2) }
                                        space()
                                        text { "活跃点" }
                                    }
                                }
                            }
                        }
                        literal("set") {
                            argument("value", Arguments.double(0.0)) { getValue ->
                                executor {
                                    val guild = DB.getGuildByGid(getGuildId())
                                    if (guild == null) {
                                        this.invoker.sendMessage {
                                            text { "公会不存在" }
                                        }
                                        return@executor
                                    }
                                    val value = getValue()
                                    DB.trans {
                                        guild.refresh()
                                        guild.vitality = value
                                        guild.flush()
                                    }
                                    this.invoker.sendMessage {
                                        text { "成功将公会" }
                                        space()
                                        mini(guild.displayName)
                                        space()
                                        text { "的活跃点设置为" }
                                        space()
                                        text { value.format(2) }
                                    }
                                }
                            }
                        }
                        literal("remove") {
                            argument("value", Arguments.double(0.0)) { getValue ->
                                executor {
                                    val guild = DB.getGuildByGid(getGuildId())
                                    if (guild == null) {
                                        this.invoker.sendMessage {
                                            text { "公会不存在" }
                                        }
                                        return@executor
                                    }
                                    val value = getValue()
                                    DB.trans {
                                        guild.refresh()
                                        guild.vitality = max(0.0, guild.vitality - value)
                                        guild.flush()
                                    }
                                    this.invoker.sendMessage {
                                        text { "成功为公会" }
                                        space()
                                        mini(guild.displayName)
                                        space()
                                        text { "移除了" }
                                        space()
                                        text { value.format(2) }
                                        space()
                                        text { "活跃点" }
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