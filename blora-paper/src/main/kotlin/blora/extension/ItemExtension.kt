@file:Suppress("UnstableApiUsage")

package blora.extension

import blora.nms.toKnbt
import io.papermc.paper.datacomponent.DataComponentTypes
import net.benwoodworth.knbt.NbtCompound
import net.kyori.adventure.text.Component
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

fun ItemStack.asDisplayName(): Component {
    val customName = this.getData(DataComponentTypes.CUSTOM_NAME)
    if (customName != null) {
        return customName
    }
    val itemName = this.getData(DataComponentTypes.ITEM_NAME)
    if (itemName != null) {
        return itemName
    }
    return Component.translatable(this)
}