@file:OptIn(ExperimentalSerializationApi::class)

package blora.mail

import blora.converter.convertGsonToKotlinxSerialization
import blora.converter.convertKotlinxToGson
import blora.json.STORE_DATA_JSON
import blora.plugin.BloraPlugin
import blora.plugin.ThirdPartys
import com.mojang.serialization.JsonOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.space
import plutoproject.adventurekt.text.text
import kotlin.collections.iterator
import net.minecraft.world.item.ItemStack as NMSItemStack

data class Attachment(
    var coins: UInt = 0u,
    var blorius: UInt = 0u,
    val items: MutableMap<ItemStack, UInt> = mutableMapOf()
) {

    fun clone(): Attachment {
        return Attachment(
            coins,
            blorius,
            items.map { it.key.clone() to it.value }
                .associate { it }
                .toMutableMap()
        )
    }

    fun isClaimableOnThisServer(): Boolean {
        return (this.coins == 0u || BloraPlugin.configuration.mail.coinsClaimable) &&
                (this.blorius == 0u || BloraPlugin.configuration.mail.bloriusClaimable) &&
                (this.items.isEmpty() || BloraPlugin.configuration.mail.itemsClaimable)
    }

    fun doPlayerHaveEnoughSpaceToClaim(player: Player): Boolean {
        if (this.items.isEmpty())
            return true
        var stacks = 0
        for ((item, amount) in this.items) {
            val realAmount = amount.toInt()
            if (realAmount > item.maxStackSize || realAmount < 0) {
                val times = (amount / item.maxStackSize.toUInt() + if (amount % item.maxStackSize.toUInt() != 0u) {
                    1u
                } else {
                    0u
                }).toInt()
                if (times < 0) // these items won't be given to player, so skip these stacks
                    continue
                stacks += times
            } else {
                stacks += 1
            }
        }
        var emptySlots = 0
        player.inventory.contents.forEach { stack ->
            if (stack == null) {
                emptySlots++
                return@forEach
            }
            if (stack.isEmpty)
                emptySlots++
        }
        return emptySlots >= stacks
    }

    fun claimToPlayer(player: Player) {
        if (coins > 0u) {
            ThirdPartys.vaultApi.depositPlayer(player, this.coins.toDouble())
        }
        if (blorius > 0u) {
            if (this.blorius.toInt() < 0) {
                val rest = blorius - Int.MAX_VALUE.toUInt()
                ThirdPartys.playerPoints.give(player.uniqueId, Int.MAX_VALUE)
                ThirdPartys.playerPoints.give(player.uniqueId, rest.toInt())
            } else {
                ThirdPartys.playerPoints.give(player.uniqueId, this.blorius.toInt())
            }
        }
        for ((item, amount) in this.items) {
            val realAmount = amount.toInt()
            if (realAmount > item.maxStackSize || realAmount < 0) {
                val times = amount / item.maxStackSize.toUInt() + if (amount % item.maxStackSize.toUInt() != 0u) {
                    1u
                } else {
                    0u
                }
                if (times.toInt() < 0) // if still too big, not give items to player
                    continue
                for (i in 0 until times.toInt()) {
                    if (i == times.toInt() - 1) {
                        player.give(item.clone().apply { this.amount = (amount % item.maxStackSize.toUInt()).toInt() })
                    } else {
                        player.give(item.clone().apply { this.amount = item.maxStackSize })
                    }
                }
            } else {
                player.give(item.clone().apply { this.amount = amount.toInt() })
            }
        }
    }

    fun itemsContainsLike(item: ItemStack): Boolean {
        val itemCopy = item.clone().apply { amount = 1 }
        return this.items.containsKey(itemCopy)
    }

    fun buildMailComponent(): Component {
        return component {
            if (coins > 0u) {
                mini(PlaceholderAPI.setPlaceholders(null, "%image_mm_icons:coin%"))
                space()
                text {
                    "x${coins}"
                }
            }
            if (blorius > 0u) {
                if (coins > 0u) {
                    space()
                    space()
                }
                mini(PlaceholderAPI.setPlaceholders(null, "%image_mm_icons:blorius%"))
                space()
                text {
                    "x${blorius}"
                }
            }
            newline()
            if (items.isNotEmpty()) {
                newline()
                newline()
                var itemsAdded = 0
                for ((item, amount) in items) {
                    itemsAdded += 1

                    mini(
                        PlaceholderAPI.setPlaceholders(
                            null, "%image_mm_items:${
                                item.type.key().value()
                            }%"
                        )
                    )
                    /*
                    if (item.type.isBlock) {
                        // TODO: block types support
                        mini(PlaceholderAPI.setPlaceholders(null, "%image_mm_items:craft_table%"))
                    } else {
                        mini(
                            PlaceholderAPI.setPlaceholders(
                                null, "%image_mm_items:${
                                    item.type.key().value()
                                }%"
                            )
                        )
                    }*/
                    space()
                    text {
                        "x${amount}"
                    }
                    space()
                    space()

                    if (itemsAdded == 5) {
                        itemsAdded = 0
                        newline()
                    }
                }
            }
        }
    }

    fun toJson(): String {
        val jsonObject = JsonObject(
            mapOf(
                "coins" to JsonPrimitive(coins),
                "blorius" to JsonPrimitive(blorius),
                "items" to JsonArray(
                    this.items.map {
                        JsonObject(
                            mapOf(
                                "item" to convertGsonToKotlinxSerialization(
                                    NMSItemStack.CODEC
                                        .encodeStart(
                                            JsonOps.INSTANCE,
                                            CraftItemStack.asNMSCopy(it.key)
                                        )
                                        .result()
                                        .get()
                                ),
                                "amount" to JsonPrimitive(it.value)
                            )
                        )
                    }
                )
            )
        )

        return STORE_DATA_JSON.encodeToString(jsonObject)
    }

    fun hasNoContent(): Boolean {
        return this.coins == 0u && this.blorius == 0u && this.items.isEmpty()
    }

    companion object {

        fun fromJson(text: String): Attachment {
            val jsonObject = STORE_DATA_JSON.decodeFromString<JsonObject>(text)
            val coins = jsonObject["coins"]?.jsonPrimitive?.int?.toUInt() ?: 0u
            val blorius = jsonObject["blorius"]?.jsonPrimitive?.int?.toUInt() ?: 0u
            val items = jsonObject["items"]!!.jsonArray.map { itemElement ->
                val itemObject = itemElement.jsonObject
                val itemStack = CraftItemStack.asBukkitCopy(
                    NMSItemStack.CODEC.decode(
                        JsonOps.INSTANCE,
                        convertKotlinxToGson(itemObject["item"]!!)
                    ).result().get().first
                )
                return@map itemStack to itemObject["amount"]!!.jsonPrimitive.int.toUInt()
            }
            return Attachment(
                coins,
                blorius,
                items.associate { it }.toMutableMap()
            )
        }

    }

}