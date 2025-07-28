@file:Suppress("UnstableApiUsage")

package blora.configuration

import blora.adventure.itemLore
import blora.item.amount
import blora.item.itemStack
import blora.item.material
import blora.item.withData
import blora.json.MINECRAFT_PRETTY_DATA_JSON
import blora.plugin.BloraPlugin
import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.style.red
import plutoproject.adventurekt.text.style.text
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with
import java.io.File

@Serializable
data class GuildVitalityShopConfiguration(
    val goods: List<GuildVitalityShopGoods> = listOf(
        GuildVitalityShopGoods(
            icon = "minecraft:diamond",
            name = "example goods",
            description = listOf(
                "first line",
                "second line",
            ),
            price = 100.0,
            content = GuildVitalityShopGoodsContent(
                coins = 1u,
                blorius = 1u,
                guildBank = 1u,
                items = listOf(
                    itemStack {
                        material { Material.APPLE }
                        amount { 24 }
                    },
                    itemStack {
                        material { Material.DIAMOND_SWORD }
                        DataComponentTypes.CUSTOM_NAME eq component {
                            text { "hahaha!" } with red.text
                        }
                        DataComponentTypes.LORE eq itemLore {
                            text { "1111" }
                            newline()
                            text { "2222" }
                        }
                        withData {
                            DataComponentTypes.UNBREAKABLE
                        }
                    }
                ),
                commands = listOf(
                    "give <player> minecraft:diamond",
                    "guild admin <guild> vitality set 1200"
                )
            ),
            enabled = false
        )
    )
) {

    companion object {

        fun loadOrCreate(): GuildVitalityShopConfiguration {
            val configurationFile = File(BloraPlugin.dataFolder, "guild_vitality_shop.json")
            if (!configurationFile.exists()) {
                configurationFile.writeText(
                    MINECRAFT_PRETTY_DATA_JSON.encodeToString(
                        GuildVitalityShopConfiguration()
                    )
                )
            }
            return MINECRAFT_PRETTY_DATA_JSON.decodeFromString(configurationFile.readText())
        }

    }

}

@Serializable
data class GuildVitalityShopGoods(
    val icon: String,
    val name: String,
    val description: List<String>,
    val price: Double,
    val content: GuildVitalityShopGoodsContent,
    val enabled: Boolean
)

@Serializable
data class GuildVitalityShopGoodsContent(
    val coins: UInt? = null,
    val blorius: UInt? = null,
    val guildBank: UInt? = null,
    val items: List<@Contextual ItemStack>? = null,
    val commands: List<String>? = null
)