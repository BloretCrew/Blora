package blora.database.mail.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object SystemMailTable : IntIdTable("blora_system_mails") {

    val identifier = text("identifier").uniqueIndex()
    val sender = text("sender").nullable() // customizable system sender name
    val title = text("title") // should be mini message parsable string
    val contents = text("contents") // should be mini message parsable string
    val triggers = text("triggers") // should be an unreadable text
    val attachment = text("attachment") // should be json
    val creator = uuid("creator")
    val sendingDate = datetime("sending_date").nullable() // this is an option customizable, null for not customizing it
    val createdAt = datetime("created_at") // this is an option customizable, null for not customizing it
    val actived = bool("actived").default(false)

}