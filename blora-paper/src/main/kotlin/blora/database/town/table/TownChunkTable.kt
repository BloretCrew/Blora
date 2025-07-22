package blora.database.town.table

import org.jetbrains.exposed.dao.id.IntIdTable

object TownChunkTable : IntIdTable("blora_town_chunks") {

    val chunkX = integer("chunk_x")
    val chunkY = integer("chunk_y")
    val townId = text("town_id")

}
