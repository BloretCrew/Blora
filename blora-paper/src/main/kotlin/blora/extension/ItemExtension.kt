package blora.extension

import blora.nms.toKnbt
import net.benwoodworth.knbt.NbtCompound
import net.minecraft.nbt.NbtOps
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import net.minecraft.world.item.ItemStack as NMSItemStack

fun ItemStack.toNBT(): NbtCompound {
    val nmsCopy = CraftItemStack.asNMSCopy(this)
    return NMSItemStack.CODEC
        .encodeStart(NbtOps.INSTANCE, nmsCopy)
        .result()
        .get()
        .toKnbt() as NbtCompound
}