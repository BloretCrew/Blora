package blora.location

import blora.database.town.dao.TownChunkDao
import com.bekvon.bukkit.residence.Residence
import com.bekvon.bukkit.residence.protection.CuboidArea
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class Aabb(
    val world: World,
    start: Vector,
    end: Vector
) {

    val start: Vector
    val end: Vector

    init {
        this.start = Vector(
            min(start.x, end.x),
            min(start.y, end.y),
            min(start.z, end.z)
        )
        this.end = Vector(
            max(start.x, end.x),
            max(start.y, end.y),
            max(start.z, end.z)
        )
    }

    fun getContainedChunks(): List<ChunkLocation> {
        val chunks = mutableSetOf<ChunkLocation>()
        for (x in floor(start.x).toInt()..ceil(end.x).toInt() step 16) {
            for (z in floor(start.z).toInt()..ceil(end.z).toInt() step 16) {
                val chunkX = if (x >= 0)
                    x / 16
                else
                    ((x + 1) / 16) - 1
                val chunkZ = if (z >= 0)
                    z / 16
                else
                    ((z + 1) / 16) - 1
                chunks.add(ChunkLocation(this.world.name, chunkX, chunkZ))
            }
        }
        return chunks.distinct()
    }

    fun containsTownChunk(): Boolean {
        for (chunk in this.getContainedChunks()) {
            if (TownChunkDao.find(chunk.world, chunk.x, chunk.z) == null) {
                return true
            }
        }
        return false
    }

    fun containsPlayerResidence(): Boolean {
        val resAabb = CuboidArea(this.start.toLocation(this.world), this.end.toLocation(this.world))
        return Residence.getInstance().residenceManager.collidesWithResidence(resAabb) != null
    }

    companion object {

        fun of(start: Location, end: Location): Aabb {
            return Aabb(
                start.world,
                start.toVector(),
                end.toVector()
            )
        }

    }

}
