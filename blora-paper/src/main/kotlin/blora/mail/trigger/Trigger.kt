package blora.mail.trigger

import org.bukkit.entity.Player

interface Trigger {

    val shouldNeverAcquirableAfterCheck: Boolean

    fun check(player: Player): Boolean

}