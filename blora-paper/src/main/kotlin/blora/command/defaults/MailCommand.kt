package blora.command.defaults

import blora.internal.api.command.*
import blora.internal.api.command.argument.Arguments
import blora.extension.localization
import blora.modules.mail.MailModule
import blora.modules.mail.createSystemMailMenu
import blora.modules.mail.mailListMenu
import blora.modules.mail.systemMailManagementMenu
import blora.permission.Permissions
import blora.plugin.BloraPlugin
import plutoproject.adventurekt.text.parsedPlaceholder

object MailCommand {

    fun register() {
        BloraCommandLib.registerCommand("mail") {
            requires {
                return@requires this.hasPermission(Permissions.Commands.Blora)
                        || this.hasPermission(Permissions.Admin)
            }

            playerExecutor { // open mails gui
                mailListMenu(this.player.asBukkit).open()
            }

            literal("system") {
                requires {
                    return@requires this.hasPermission(Permissions.Commands.Blora)
                }

                playerExecutor {
                    systemMailManagementMenu(this.player.asBukkit).open()
                }

                literal("create") {

                    playerExecutor {
                        createSystemMailMenu(this.player.asBukkit).open()
                    }
                }

                literal("delete") {
                    requires {
                        return@requires this.isConsole
                                || (this.isPlayer && (
                                this.player().asBukkit.name.lowercase() == "rhedar"
                                        || this.player().asBukkit.name.lowercase() == "xupipi_cn"
                                ))
                    }


                    argument("systemMailId", Arguments.word) { getSystemMailId ->
                        suggests {
                            BloraPlugin.database.listSystemMails().map { it.identifier }.forEach(this::suggest)
                        }

                        executor {
                            val systemMailId = getSystemMailId()

                            val systemMail = BloraPlugin.database.getSystemMailByIdentifier(systemMailId)

                            if (systemMail == null) {
                                this.invoker.sendMessage {
                                    localization(
                                        player = if (this@executor.isPlayer) {
                                            this@executor.player.asBukkit
                                        } else {
                                            null
                                        },
                                        tags = {
                                            parsedPlaceholder("id", systemMailId)
                                        }
                                    ) {
                                        this.commandMailErrorSystem_mail_not_exists
                                    }
                                }
                                return@executor
                            }

                            BloraPlugin.database.deleteSystemMailById(systemMailId)
                            this.invoker.sendMessage {
                                localization(
                                    player = if (this@executor.isPlayer) this@executor.player.asBukkit else null,
                                    tags = {
                                        parsedPlaceholder("id", systemMailId)
                                    }
                                ) {
                                    this.commandMailSuccessDelete_mail
                                }
                            }
                        }
                    }
                }

                literal("send") {

                    argument("player", Arguments.player) { getPlayer ->

                        argument("systemMailId", Arguments.word) { getSystemMailId ->
                            suggests {
                                BloraPlugin.database.listSystemMails().map { it.identifier }.forEach(this::suggest)
                            }

                            executor {
                                val player = getPlayer()
                                val systemMailId = getSystemMailId()

                                val systemMail = BloraPlugin.database.getSystemMailByIdentifier(systemMailId)

                                if (systemMail == null) {
                                    this.invoker.sendMessage {
                                        localization(
                                            player = if (this@executor.isPlayer) {
                                                this@executor.player.asBukkit
                                            } else {
                                                null
                                            },
                                            tags = {
                                                parsedPlaceholder("id", systemMailId)
                                            }
                                        ) {
                                            this.commandMailErrorSystem_mail_not_exists
                                        }
                                    }
                                    return@executor
                                }
                                if (BloraPlugin.database.isPlayerReceivedSystemMail(player.uniqueId, systemMailId)) {
                                    this.invoker.sendMessage {
                                        localization(
                                            player = if (this@executor.isPlayer) {
                                                this@executor.player.asBukkit
                                            } else {
                                                null
                                            },
                                            tags = {
                                                parsedPlaceholder("player", player.name)
                                                parsedPlaceholder("id", systemMailId)
                                            }
                                        ) {
                                            this.commandMailErrorPlayer_received_mail
                                        }
                                    }
                                    return@executor
                                }

                                MailModule.receiveNewMail(player, systemMail, visible = true, notify = true)
                            }
                        }
                    }
                }
            }
        }
    }

}