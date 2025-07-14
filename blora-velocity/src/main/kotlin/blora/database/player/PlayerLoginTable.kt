package blora.database.player

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object PlayerLoginTable : IntIdTable("blora_player_login") {

    val uuid = uuid("player_uuid")
    val date = date("login_date")

}