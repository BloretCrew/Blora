package blora.database.mail

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object MailTable : IntIdTable("blora_mails") {

    val receiver = uuid("receiver")
    val sender = text("sender") // 3 types: player:uuid, guild:guildId, system
    val title = text("title") // should be mini message parsable string
    val contents = text("contents") // should be mini message parsable string
    val attachment = text("attachment") // should be json
    val creator = uuid("creator")
    val createdAt = datetime("created_at") // for system mail, this will be set to system mail specific date
    val systemMailId = text("system_mail_id").nullable() // only system mail has this
    val visible = bool("visible").default(true) // because system mail trigger check may be set to unacquirable
    val isRead = bool("is_read").default(false)
    val isClaim = bool("is_claim").default(false)

}