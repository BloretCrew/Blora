@file:Suppress("UnstableApiUsage")

package blora.guild.menu.guildview.guildvitalityshop

import blora.configuration.CONF
import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.format
import blora.extension.localization
import blora.formula.FormulaTokenizer
import blora.formula.FormulaVariable
import blora.guild.dataprovider.GuildVitalityShopGoodsDataProvider
import blora.guild.formula.GuildFormulaVariable
import blora.item.type
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.*
import blora.plugin.BloraPlugin
import blora.plugin.ThirdPartys
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Registry
import org.bukkit.inventory.ItemType
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildVitalityShopMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    return pageableMenuPage(menu, GuildVitalityShopGoodsDataProvider()) {
        pageId {
            "guild_${guildId}_vitalityShop"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_vitality_shopTitle
            }
        }
        showBackButton()
        dataItem { viewContext, goods, dataIndex ->
            icon {
                type {
                    Registry.ITEM.get(Key.key(goods.icon)) ?: ItemType.STONE
                }
            }
            name {
                localization(viewContext.viewer) {
                    goods.name
                }
            }
            description {
                for (descriptionLine in goods.description) {
                    newline()
                    localization(viewContext.viewer) {
                        descriptionLine
                    }
                }
                if (goods.description.isNotEmpty())
                    newline()
                newline()
                localization(
                    player = viewContext.viewer,
                    tags = {
                        parsedPlaceholder("price", goods.price.format(2))
                    }
                ) {
                    this.guild.menu.menuGuild_vitality_shopItemDescriptionPrice
                }
            }
            clickEvent { clickContext ->
                DB.trans {
                    guild.refresh()
                }
                if (guild.vitality < goods.price) {
                    clickContext.viewer.send {
                        localization(clickContext.viewer) {
                            this.guild.guildVitality_shopVitality_not_enough
                        }
                    }
                    return@clickEvent
                }
                DB.trans {
                    guild.vitality -= goods.price
                    if (goods.content.guildBank != null) {
                        guild.bankBalance += goods.content.guildBank.toDouble()

                        if (guild.bankBalance > guild.bankBalanceMax) {
                            val guildVariable = GuildFormulaVariable(guild)
                            val delta = guild.bankBalance - guild.bankBalanceMax
                            guild.bankBalanceMax = guild.bankBalance
                            val formula = FormulaTokenizer.parse(CONF.guild.vitality.bankBalanceNewMaxVitalityFormula)
                            if (formula == null) {
                                BloraPlugin.slF4JLogger.error("公会银行最大值更新计算公式无法正常解析(1)，请调整后重启服务器")
                            } else {
                                val value = formula.calculate(guildVariable, FormulaVariable.simple("delta", delta))
                                if (value == null) {
                                    BloraPlugin.slF4JLogger.error("公会银行最大值更新计算公式无法正常解析(2)，请调整后重启服务器")
                                } else {
                                    guild.vitality += value
                                    guild.flush()
                                    Bukkit.getOnlinePlayers()
                                        .filter { player -> guild.members.contains(player.uniqueId) }
                                        .forEach { player ->
                                            player.send {
                                                localization(
                                                    player = player,
                                                    tags = {
                                                        parsedPlaceholder("guild", guild.displayName)
                                                        parsedPlaceholder("vitality", value.format(2))
                                                        componentPlaceholder("reason") {
                                                            localization(
                                                                player,
                                                                tags = {
                                                                    parsedPlaceholder(
                                                                        "player",
                                                                        clickContext.viewer.name
                                                                    )
                                                                }
                                                            ) {
                                                                this.guild.guildVitalityAddReasonBank_new_max_balance_from_vitality_shop
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    this.guild.guildVitalityAdd
                                                }
                                            }
                                        }
                                }
                            }
                        }
                    }
                    guild.flush()
                }
                if (goods.content.coins != null) {
                    ThirdPartys.vaultApi.depositPlayer(clickContext.viewer, goods.content.coins.toDouble())
                }
                if (goods.content.blorius != null) {
                    ThirdPartys.playerPoints.give(clickContext.viewer.uniqueId, goods.content.blorius.toInt())
                }
                if (goods.content.items != null && goods.content.items.isNotEmpty()) {
                    for (item in goods.content.items) {
                        clickContext.viewer.inventory.addItem(item)
                            .values
                            .forEach { moreItem ->
                                clickContext.viewer.world.dropItem(
                                    clickContext.viewer.location,
                                    moreItem,
                                )
                            }
                    }
                }
                if (goods.content.commands != null && goods.content.commands.isNotEmpty()) {
                    for (command in goods.content.commands) {
                        Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            command.replace("<player>", clickContext.viewer.name)
                                .replace("<guild>", guild.gid)
                        )
                    }
                }
                clickContext.viewer.send {
                    localization(
                        player = clickContext.viewer,
                        tags = {
                            parsedPlaceholder("vitality", goods.price.format(2))
                            parsedPlaceholder("goods", goods.name)
                        }
                    ) {
                        this.guild.guildVitality_shopSuccess
                    }
                }
            }
        }
    }
}