package blora.modules.mail.trigger

import blora.plugin.BloraPlugin
import org.bukkit.entity.Player
import java.time.LocalDate

class OnlineInRecentDaysTrigger(
    var anchorDate: LocalDate,
    var days: Int
) : Trigger {

    override val shouldNeverAcquirableAfterCheck
        get() = anchorDate < LocalDate.now()

    override fun check(player: Player): Boolean {
        return BloraPlugin.database.getPlayerLoginData(player.uniqueId)
            .isLoginInDays(this.anchorDate, this.days.toLong())
    }

}