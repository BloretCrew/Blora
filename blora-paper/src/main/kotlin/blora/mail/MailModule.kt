package blora.mail

import blora.database.mail.dao.MailDao
import blora.database.mail.dao.SystemMailDao
import blora.database.redeem.dao.RedeemDao
import blora.extension.localization
import blora.extension.openDialog
import blora.localization.i18n
import blora.plugin.BloraPlugin
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.style.callback
import plutoproject.adventurekt.text.style.showText
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with
import java.time.LocalDateTime

object MailModule {

    fun receiveNewMail(player: Player, systemMail: SystemMailDao, visible: Boolean, notify: Boolean) {
        if (BloraPlugin.database.isPlayerReceivedSystemMail(player.uniqueId, systemMail.identifier))
            return
        val mail = BloraPlugin.database.trans {
            MailDao.new {
                this.receiver = player.uniqueId
                this.parsedSender = Sender.System(systemMail.sender ?: "")
                this.title = systemMail.title
                this.contents = systemMail.contents
                this.attachment = systemMail.attachment
                this.creator = systemMail.creator
                this.createdAt = systemMail.sendingDate ?: LocalDateTime.now()
                this.systemMailId = systemMail.identifier
                this.visible = visible
                this.isRead = false
                this.isClaim = systemMail.parsedAttachment.hasNoContent()
            }
        }
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

    fun receiveRedeemMail(player: Player, redeem: RedeemDao, notify: Boolean) {
        val mail = BloraPlugin.database.trans {
            MailDao.new {
                this.receiver = player.uniqueId
                this.parsedSender = Sender.System(i18n(player) { this.redeem.redeemMailSender })
                this.title = i18n(player) { this.redeem.redeemMailTitle }
                this.contents = i18n(player) { this.redeem.redeemMailContents }.replace("<redeem>", redeem.code)
                this.parsedAttachment = redeem.parseAttachment
                this.creator = redeem.creator
                this.createdAt = LocalDateTime.now()
                this.systemMailId = null
                this.visible = true
                this.isRead = false
                this.isClaim = redeem.parseAttachment.hasNoContent()
            }
        }
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