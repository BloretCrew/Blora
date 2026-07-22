package blora.mail

import blora.database.mail.dao.SystemMailDao
import blora.extension.localization
import blora.extension.openDialog
import blora.plugin.BloraPlugin
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.style.callback
import plutoproject.adventurekt.text.style.showText
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with

object MailModule {

    fun receiveNewMail(player: Player, systemMail: SystemMailDao, visible: Boolean, notify: Boolean) {
        val mail = BloraPlugin.database.tryReceiveSystemMail(
            player = player.uniqueId,
            systemMailId = systemMail.identifier,
            visible = visible
        ) ?: return
        if (notify) {
            player.send {
                localization(player) {
                    this.mail.mailNotify
                } with callback {
                    BloraPlugin.database.trans {
                        mail.isRead = true
                        mail.flush()
                    }
                    player.openDialog(
                        playerViewMail(player, mail)
                    )
                } with showText {
                    localization(
                        player = player,
                        tags = {
                            componentPlaceholder("title") {
                                localization(player) {
                                    mail.title
                                }
                            }
                            componentPlaceholder("sender") {
                                val sender = mail.parsedSender
                                when (sender) {
                                    is Sender.Player -> {
                                        text {
                                            BloraPlugin.database.getPlayerDisplayName(sender.uuid)
                                        }
                                    }

                                    is Sender.System -> {
                                        localization(player) {
                                            sender.name.ifEmpty {
                                                this.mail.mailSenderSystemDefault
                                            }
                                        }
                                    }

                                    else -> {
                                        localization(player) {
                                            this.mail.mailSenderUnknown
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        this.mail.mailNotifyHover
                    }
                }
            }
        }
    }

}
