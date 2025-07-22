package blora.mail.trigger

import blora.plugin.BloraPlugin
import org.bukkit.entity.Player
import java.time.LocalDate

class OnlineInRangeTrigger(
    var from: LocalDate,
    var to: LocalDate,
) : Trigger {

    override val shouldNeverAcquirableAfterCheck
        get() = from < LocalDate.now()

    override fun check(player: Player): Boolean {
        return BloraPlugin.database.getPlayerLoginData(player.uniqueId).anyIn(this.from, this.to)
    }

}