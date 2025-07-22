package blora.database.mail.dao

import blora.database.mail.table.MailTable
import blora.mail.Attachment
import blora.mail.Sender
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime
import java.util.*

class MailDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<MailDao>(MailTable)

    var receiver: UUID by MailTable.receiver
    var sender: String by MailTable.sender
    var title: String by MailTable.title
    var contents: String by MailTable.contents
    var attachment: String by MailTable.attachment
    var creator: UUID by MailTable.creator // keep this, redeem still has creator, but no system mail id
    var createdAt: LocalDateTime by MailTable.createdAt
    var systemMailId: String? by MailTable.systemMailId
    var visible: Boolean by MailTable.visible
    var isRead: Boolean by MailTable.isRead
    var isClaim: Boolean by MailTable.isClaim

    var parsedSender: Sender
        get() = Sender.deserialize(this.sender)
        set(value) {
            this.sender = value.serialize()
        }

    var parsedAttachment: Attachment
        get() {
            return Attachment.fromJson(this.attachment)
        }
        set(value) {
            this.attachment = value.toJson()
        }

}