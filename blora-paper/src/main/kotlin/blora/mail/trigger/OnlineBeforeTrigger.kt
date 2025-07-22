package blora.mail.trigger

import blora.plugin.BloraPlugin
import org.bukkit.entity.Player
import java.time.LocalDate

class OnlineBeforeTrigger(
    var date: LocalDate
) : Trigger {

    override val shouldNeverAcquirableAfterCheck
        get() = date < LocalDate.now()

    override fun check(player: Player): Boolean {
        return BloraPlugin.database.getPlayerLoginData(player.uniqueId).anyBefore(this.date)
    }

}