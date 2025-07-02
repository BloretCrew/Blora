package blora.database.table

import org.jetbrains.exposed.sql.Table

object UserTable : Table("users") {

    val uid = integer("uid").autoIncrement()
    val uuid = uuid("uuid")
    val name = text("name")

}