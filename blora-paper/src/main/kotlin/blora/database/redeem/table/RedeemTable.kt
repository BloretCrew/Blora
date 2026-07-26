package blora.database.redeem.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object RedeemTable : IntIdTable("blora_redeem_codes") {

    val code = text("code").uniqueIndex()
    val attachment = text("attachment")
    val creator = uuid("creator")
    val createdAt = datetime("created_at")
    val oneUse = bool("one_use").default(false)
    val used = bool("used").default(false)

}