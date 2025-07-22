package blora.converter

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import net.minecraft.world.item.ItemStack as NMSItemStack

fun ItemStack.toJson(): String {
    return Gson().toJson(
        NMSItemStack.CODEC.encodeStart(JsonOps.INSTANCE, CraftItemStack.asNMSCopy(this))
            .result()
            .get()
    )
}

fun String.jsonToItemStack(): ItemStack {
    return CraftItemStack.asBukkitCopy(
        NMSItemStack.CODEC.decode(
            JsonOps.INSTANCE,
            JsonParser.parseString(this)
        )
            .result()
            .get()
            .first
    )
}