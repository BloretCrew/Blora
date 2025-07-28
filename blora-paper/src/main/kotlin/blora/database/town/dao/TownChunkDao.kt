package blora.database.town.dao

import blora.database.DB
import blora.database.town.table.TownChunkTable
import blora.path.path2d.AStarPathfinder2D
import blora.path.path2d.PathPoint2D
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

class TownChunkDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<TownChunkDao>(TownChunkTable) {

        fun listByTown(town: String): List<TownChunkDao> {
            return DB.trans {
                find {
                    TownChunkTable.townId eq town
                }.toList()
            }
        }

        fun listByGuild(guild: String): List<TownChunkDao> {
            return DB.trans {
                find {
                    TownChunkTable.guildId eq guild
                }.toList()
            }
        }

        fun find(world: String, x: Int, z: Int): TownChunkDao? {
            return DB.trans {
                find {
                    TownChunkTable.chunkX eq x and
                            (TownChunkTable.chunkZ eq z) and
                            (TownChunkTable.world eq world)
                }.firstOrNull()
            }
        }

    }

    var townId by TownChunkTable.townId
    var guildId by TownChunkTable.guildId

    var world by TownChunkTable.world
    var chunkX by TownChunkTable.chunkX
    var chunkZ by TownChunkTable.chunkZ

    fun checkHavePassToCenter(
        centerChunkX: Int,
        centerChunkZ: Int,
        exceptChunkX: Int,
        exceptChunkZ: Int,
    ): Boolean {
        return AStarPathfinder2D.findPath(
            PathPoint2D.of(centerChunkX, centerChunkZ),
            PathPoint2D.of(this.chunkX, this.chunkZ)
        ) { point ->
            if (point.x == centerChunkX && point.y == centerChunkZ)
                true
            else if (point.x == exceptChunkX && point.y == exceptChunkZ)
                false
            else {
                val townChunk = find(this.world, point.x, point.y)
                townChunk != null && townChunk.townId == this.townId
            }
        } != null
    }

}