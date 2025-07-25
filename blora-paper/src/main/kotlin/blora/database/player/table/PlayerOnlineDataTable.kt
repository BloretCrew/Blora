package blora.database.player.table

import blora.json.STORE_DATA_JSON
import blora.player.PlayerOnlineData
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.json.json

object PlayerOnlineDataTable : IntIdTable("blora_player_online_data") {

    val uuid = uuid("player_uuid")
    val online = json<PlayerOnlineData>("online_data", STORE_DATA_JSON)
}