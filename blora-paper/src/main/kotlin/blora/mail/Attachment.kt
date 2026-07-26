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
import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.space
import plutoproject.adventurekt.text.text
import kotlin.math.min
import net.minecraft.world.item.ItemStack as NMSItemStack

data class Attachment(
    var coins: UInt = 0u,
    var blorius: UInt = 0u,
    val items: MutableList<Pair<ItemStack, UInt>> = mutableListOf()
) {

    fun clone(): Attachment {
        return Attachment(
            coins,
            blorius,
            items.map { it.first.clone() to it.second }
                .toMutableList()
        )
    }

    fun isClaimableOnThisServer(): Boolean {
        return (this.coins == 0u || BloraPlugin.configuration.mail.coinsClaimable) &&
            (this.blorius == 0u || BloraPlugin.configuration.mail.bloriusClaimable) &&
            (this.items.isEmpty() || BloraPlugin.configuration.mail.itemsClaimable)
    }

    /**
     * Simulate packing items into a clone of storage contents (main inventory only).
     */
    fun doPlayerHaveEnoughSpaceToClaim(player: Player): Boolean {
        if (this.items.isEmpty())
            return true
        val storage = player.inventory.storageContents.map { it?.clone() }.toTypedArray()
        for ((template, amountU) in this.items) {
            var remaining = amountU.toLong()
            if (remaining <= 0L) continue
            val maxStack = template.maxStackSize.coerceAtLeast(1)
            // Fill existing stacks first
            for (i in storage.indices) {
                if (remaining <= 0L) break
                val slot = storage[i] ?: continue
                if (slot.isEmpty) continue
                if (!slot.isSimilar(template)) continue
                val free = maxStack - slot.amount
                if (free <= 0) continue
                val add = min(free.toLong(), remaining).toInt()
                slot.amount += add
                remaining -= add
            }
            // Then empty slots
            for (i in storage.indices) {
                if (remaining <= 0L) break
                val slot = storage[i]
                if (slot != null && !slot.isEmpty) continue
                val chunk = min(maxStack.toLong(), remaining).toInt()
                storage[i] = template.clone().apply { amount = chunk }
                remaining -= chunk
            }
            if (remaining > 0L) {
                return false
            }
        }
        return true
    }

    fun claimToPlayer(player: Player) {
        if (coins > 0u) {
            val response = ThirdPartys.vaultApi.depositPlayer(player, this.coins.toDouble())
            if (!response.transactionSuccess()) {
                throw IllegalStateException("Vault deposit failed: ${response.errorMessage}")
            }
        }
        if (blorius > 0u) {
            var remaining = blorius.toLong()
            while (remaining > 0L) {
                val chunk = min(remaining, Int.MAX_VALUE.toLong()).toInt()
                val ok = ThirdPartys.playerPoints.give(player.uniqueId, chunk)
                if (!ok) {
                    throw IllegalStateException(
                        "PlayerPoints give failed for ${player.uniqueId} amount=$chunk remaining=$remaining"
                    )
                }
                remaining -= chunk
            }
        }
        for ((item, amountU) in this.items) {
            var remaining = amountU.toLong()
            if (remaining <= 0L) continue
            val maxStack = item.maxStackSize.coerceAtLeast(1).toLong()
            while (remaining > 0L) {
                val chunk = min(remaining, maxStack).toInt()
                player.give(item.clone().apply { this.amount = chunk })
                remaining -= chunk
            }
        }
    }

    fun itemsContainsLike(item: ItemStack): Boolean {
        return this.items.any { it.first.isSimilar(item) }
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

                    val customId = CraftEngineItems.getCustomItemId(item)
                    if (CraftEngineItems.isCustomItem(item) && customId != null) {
                        mini(
                            PlaceholderAPI.setPlaceholders(
                                null, "%image_mm_items:${
                                    customId.value()
                                }%"
                            )
                        )
                    } else {
                        mini(
                            PlaceholderAPI.setPlaceholders(
                                null, "%image_mm_items:${
                                    item.type.key().value()
                                }%"
                            )
                        )
                    }
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
                "coins" to JsonPrimitive(coins.toLong()),
                "blorius" to JsonPrimitive(blorius.toLong()),
                "items" to JsonArray(
                    this.items.map {
                        JsonObject(
                            mapOf(
                                "item" to convertGsonToKotlinxSerialization(
                                    NMSItemStack.CODEC
                                        .encodeStart(
                                            JsonOps.INSTANCE,
                                            CraftItemStack.asNMSCopy(it.first)
                                        )
                                        .result()
                                        .get()
                                ),
                                "amount" to JsonPrimitive(it.second.toLong())
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

        private const val MAX_AMOUNT = 1_000_000_000L

        fun fromJson(text: String): Attachment {
            val jsonObject = STORE_DATA_JSON.decodeFromString<JsonObject>(text)
            val coins = parseAmount(jsonObject["coins"])
            val blorius = parseAmount(jsonObject["blorius"])
            val items = jsonObject["items"]?.jsonArray?.mapNotNull { itemElement ->
                val itemObject = itemElement.jsonObject
                val itemNode = itemObject["item"] ?: return@mapNotNull null
                val decoded = NMSItemStack.CODEC.decode(
                    JsonOps.INSTANCE,
                    convertKotlinxToGson(itemNode)
                ).result()
                if (decoded.isEmpty) return@mapNotNull null
                val itemStack = CraftItemStack.asBukkitCopy(decoded.get().first)
                val amount = parseAmount(itemObject["amount"])
                if (amount == 0u) return@mapNotNull null
                itemStack to amount
            }?.toMutableList() ?: mutableListOf()
            return Attachment(
                coins,
                blorius,
                items
            )
        }

        private fun parseAmount(element: JsonElement?): UInt {
            if (element == null) return 0u
            val primitive = element.jsonPrimitive
            val longVal = primitive.longOrNull
                ?: primitive.contentOrNull?.toLongOrNull()
                ?: return 0u
            if (longVal < 0L || longVal > MAX_AMOUNT) return 0u
            return longVal.toUInt()
        }

    }

}
