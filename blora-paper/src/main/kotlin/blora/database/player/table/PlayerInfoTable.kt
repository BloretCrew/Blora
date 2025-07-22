package blora.database.player.table

import org.jetbrains.exposed.dao.id.IntIdTable

object PlayerInfoTable : IntIdTable("blora_player_info") {

    val uuid = uuid("player_uuid")
    val name = text("username")

}