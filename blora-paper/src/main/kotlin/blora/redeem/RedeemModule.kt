package blora.redeem

import blora.extension.localization
import blora.extension.openDialog
import blora.localization.i18n
import blora.mail.playerViewMail
import blora.plugin.BloraPlugin
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.style.callback
import plutoproject.adventurekt.text.style.showText
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with

object RedeemModule {

    fun redeemCode(player: Player, code: String): RedeemResult {
        val codeKey = code.lowercase()
        val title = i18n(player) { this.redeem.redeemMailTitle }
        val contents = i18n(player) { this.redeem.redeemMailContents }.replace("<redeem>", codeKey)
        val senderName = i18n(player) { this.redeem.redeemMailSender }
        val result = BloraPlugin.database.redeemCode(
            player.uniqueId,
            codeKey,
            title,
            contents,
            senderName
        )
        if (result is RedeemResult.Success) {
            val mail = result.mail
            player.send {
                localization(player) {
                    this.mail.mailNotify
                } with callback {
                    BloraPlugin.database.trans {
                        mail.isRead = true
                        mail.flush()
                    }
                    player.openDialog(playerViewMail(player, mail))
                } with showText {
                    localization(
                        player = player,
                        tags = {
                            componentPlaceholder("title") {
                                text { mail.title }
                            }
                            componentPlaceholder("sender") {
                                localization(player) {
                                    senderName.ifEmpty { this.mail.mailSenderSystemDefault }
                                }
                            }
                        }
                    ) {
                        this.mail.mailNotifyHover
                    }
                }
            }
        }
        return result
    }

}
