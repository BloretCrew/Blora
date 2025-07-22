package blora.mail

import blora.mail.trigger.Trigger
import java.time.LocalDateTime

data class CreatingMailContext(
    var modifying: Boolean = false,
    var title: String = "",
    var sender: String = "",
    var contents: String = "",
    val triggers: MutableSet<Trigger> = mutableSetOf(),
    var attachment: Attachment = Attachment(),
    var date: LocalDateTime? = null
)
