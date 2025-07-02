package blora.api.position

import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import blora.api.extension.eyeHeight

enum class EntityAnchor(
    private val transform: (vector: Vector, entity: Entity) -> Vector
) {

    FEET({ vector, _ -> vector }),
    EYES({ vector, entity -> Vector(vector.x, vector.y + entity.eyeHeight, vector.z) });

    fun apply(entity: Entity): Vector {
        return this.transform(entity.location.toVector(), entity)
    }

}