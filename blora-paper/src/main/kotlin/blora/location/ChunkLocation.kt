package blora.location

import blora.database.town.dao.TownChunkDao

data class ChunkLocation(
    val world: String,
    val x: Int,
    val z: Int
) {

    fun getClaimedTown(): TownChunkDao? {
        return TownChunkDao.find(this.world, this.x, this.z)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is ChunkLocation) return false
        return this.world == other.world && this.x == other.x && this.z == other.z
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + z
        result = 31 * result + world.hashCode()
        return result
    }

}
