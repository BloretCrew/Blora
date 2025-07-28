package blora.database.town.table

import blora.json.STORE_DATA_JSON
import blora.town.TownPermissionContainer
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.json.JsonColumnType

object TownTable : IntIdTable("blora_towns") {

    val townId = text("town_id")
    val guildId = text("guild_id")

    val icon = text("icon")
    val displayName = text("display_name")

    val welcomeMessage = text("welcome_message").nullable().default(null)
    val goodbyeMessage = text("goodbye_message").nullable().default(null)

    val world = text("world")
    val centerChunkX = integer("center_chunk_x")
    val centerChunkZ = integer("center_chunk_z")

    val creator = uuid("creator")
    val createdAt = datetime("created_at")

    val permissionContainers = array(
        "permission_containers", JsonColumnType<TownPermissionContainer>(
            { STORE_DATA_JSON.encodeToString(it) },
            { STORE_DATA_JSON.decodeFromString(it) },
        )
    )

}