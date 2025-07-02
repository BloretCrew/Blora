package blora.nms

import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.entity.Entity
import net.minecraft.world.entity.Entity as NMSEntity

fun Entity.nms(): NMSEntity {
    return (this as CraftEntity).handle
}