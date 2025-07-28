package blora.database.town.table

import org.jetbrains.exposed.dao.id.IntIdTable

object TownChunkTable : IntIdTable("blora_town_chunks") {

    val townId = text("town_id")
    val guildId = text("guild_id")

    val world = text("world")
    val chunkX = integer("chunk_x")
    val chunkZ = integer("chunk_z")

}
