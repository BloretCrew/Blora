package blora.modules.mail.database

import org.jetbrains.exposed.dao.id.IntIdTable

object MailTable : IntIdTable("blora_mails") {

    val uuid = uuid("receiver")
    val sender = text("sender") // 3 types: player:uuid, guild:guildId, system
    val contents = text("contents")
    val attachment = text("attachment")

}