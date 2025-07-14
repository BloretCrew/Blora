package blora.modules.mail

import blora.database.mail.MailDao
import blora.database.mail.SystemMailDao
import blora.database.redeem.RedeemDao
import blora.extension.localization
import blora.extension.openDialog
import blora.localization.i18n
import blora.modules.Module
import blora.plugin.BloraPlugin
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.style.callback
import plutoproject.adventurekt.text.style.rgb
import plutoproject.adventurekt.text.style.showText
import plutoproject.adventurekt.text.style.text
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with
import java.time.LocalDateTime

object MailModule : Module {

    override val id: String = "mail"
    override val name: String = "Mail"
    override val displayName: Component = component {
        text("邮件") with rgb(137, 129, 124).text
    }
    override var enabled: Boolean = false
    override val dependencies: List<String> = listOf()

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
                    this.mailNotify
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
                                                this.mailSenderSystemDefault
                                            }
                                        }
                                    }

                                    else -> {
                                        localization(player) {
                                            this.mailSenderUnknown
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        this.mailNotifyHover
                    }
                }
            }
        }
    }

    fun receiveRedeemMail(player: Player, redeem: RedeemDao, notify: Boolean) {
        val mail = BloraPlugin.database.trans {
            MailDao.new {
                this.receiver = player.uniqueId
                this.parsedSender = Sender.System(i18n(player) { this.redeemMailSender })
                this.title = i18n(player) { this.redeemMailTitle }
                this.contents = i18n(player) { this.redeemMailContents }.replace("<redeem>", redeem.code)
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
                    this.mailNotify
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
                                                this.mailSenderSystemDefault
                                            }
                                        }
                                    }

                                    else -> {
                                        localization(player) {
                                            this.mailSenderUnknown
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        this.mailNotifyHover
                    }
                }
            }
        }
    }

}