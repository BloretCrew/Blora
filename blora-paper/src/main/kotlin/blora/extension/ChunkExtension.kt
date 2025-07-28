package blora.extension

import blora.database.town.dao.TownChunkDao
import com.bekvon.bukkit.residence.Residence
import com.bekvon.bukkit.residence.protection.CuboidArea
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.util.Vector

fun Chunk.smallestPoint(): Location {
    return Vector(
        this.x * 16,
        this.world.minHeight,
        this.z * 16,
    ).toLocation(this.world)
}

fun Chunk.biggestPoint(): Location {
    return Vector(
        (this.x + 1) * 16 - 1,
        this.world.maxHeight,
        (this.z + 1) * 16 - 1,
    ).toLocation(this.world)
}

fun Chunk.getSurroundingTownChunks(): List<TownChunkDao> {
    return listOf(
        this.x - 1 to this.z,
        this.x to this.z - 1,
        this.x + 1 to this.z,
        this.x to this.z + 1,
    ).mapNotNull {
        TownChunkDao.find(this.world.name, it.first, it.second)
    }
}

fun Chunk.containsPlayerResidence(): Boolean {
    val resAabb = CuboidArea(this.smallestPoint(), this.biggestPoint())
    return Residence.getInstance().residenceManager.collidesWithResidence(resAabb) != null
}

fun Chunk.isClaimedByAnyTown(): Boolean {
    return TownChunkDao.find(this.world.name, this.x, this.z) != null
}

fun Chunk.getClaimedTown(): TownChunkDao? {
    return TownChunkDao.find(this.world.name, this.x, this.z)
}