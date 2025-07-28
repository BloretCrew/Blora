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

    fun getContainedChunks(): List<Chunk> {
        val chunks = mutableSetOf<Chunk>()
        for (x in floor(start.x).toInt()..ceil(end.x).toInt()) {
            for (z in floor(start.z).toInt()..ceil(end.z).toInt()) {
                chunks.add(this.world.getChunkAt(x, z))
            }
        }
        return chunks.toList()
    }

    fun containsTownChunk(): Boolean {
        for (chunk in this.getContainedChunks()) {
            if (TownChunkDao.find(chunk.world.name, chunk.x, chunk.z) == null) {
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