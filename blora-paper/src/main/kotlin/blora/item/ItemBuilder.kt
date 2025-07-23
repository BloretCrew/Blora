@file:Suppress("UnstableApiUsage")

package blora.item

import io.papermc.paper.datacomponent.DataComponentType
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType

class ItemBuilder {

    internal var itemStack: ItemStack? = null

    infix fun <T : Any> DataComponentType.Valued<T>.eq(value: T) {
        this@ItemBuilder.itemStack?.setData(this, value)
    }

    fun build(): ItemStack? {
        return itemStack
    }

}

fun ItemBuilder.clone(value: () -> ItemStack?) {
    this.itemStack = value()
}

fun ItemBuilder.type(value: () -> ItemType) {
    this.itemStack = value().createItemStack()
}

fun ItemBuilder.material(material: () -> Material) {
    this.itemStack = ItemStack(material())
}

fun ItemBuilder.amount(value: () -> Int) {
    this.itemStack?.amount = value()
}

fun itemStack(builder: ItemBuilder.() -> Unit): ItemStack? {
    return ItemBuilder().apply(builder).build()
}