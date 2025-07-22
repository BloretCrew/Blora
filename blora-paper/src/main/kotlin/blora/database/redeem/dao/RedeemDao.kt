package blora.database.redeem.dao

import blora.database.redeem.table.RedeemTable
import blora.mail.Attachment
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class RedeemDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<RedeemDao>(RedeemTable)

    var code by RedeemTable.code
    var attachment by RedeemTable.attachment
    var creator by RedeemTable.creator
    var createdAt by RedeemTable.createdAt
    var oneUse by RedeemTable.oneUse
    var used by RedeemTable.used

    var parseAttachment: Attachment
        get() = Attachment.fromJson(this.attachment)
        set(value) {
            this.attachment = value.toJson()
        }

}