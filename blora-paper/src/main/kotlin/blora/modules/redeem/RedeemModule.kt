package blora.modules.redeem

import blora.modules.mail.MailModule
import blora.plugin.BloraPlugin
import org.bukkit.entity.Player

object RedeemModule {

    fun redeemCode(player: Player, code: String) {
        val redeem = BloraPlugin.database.getRedeemByCode(code)
        if (redeem == null)
            return
        BloraPlugin.database.redeemCode(player.uniqueId, code)
        MailModule.receiveRedeemMail(player, redeem, true)
    }

}