package blora.guild.menu.guildview.guildbank

import blora.configuration.CONF
import blora.database.DB
import blora.database.guild.dao.GuildBloriusToVitalityTimesDao
import blora.database.guild.dao.GuildDao
import blora.extension.format
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.dialog.guildview.guildbank.guildBank_storeDialog
import blora.guild.dialog.guildview.guildbank.guildBank_withdrawDialog
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.LimitedDynamicMenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.limitedDynamicMenuPage
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.title
import blora.plugin.ThirdPartys
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildBankMenu(menu: Menu, guild: GuildDao): LimitedDynamicMenuPage {
    val guildId = guild.id
    return limitedDynamicMenuPage(menu) {
        pageId {
            "guild_${guildId}_bank"
        }
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_bankTitle
            }
        }
        backButton()

        val canExchangeBlorius =
            CONF.guild.vitality.bloriusExchangeValue > 0 && CONF.guild.vitality.bloriusExchangePrice > 0 && CONF.guild.vitality.bloriusExchangeMaxTimes > 0

        val storeButtonPosition = if (canExchangeBlorius) {
            3 to 2
        } else {
            3 to 3
        }

        val withdrawButtonPosition = if (canExchangeBlorius) {
            3 to 8
        } else {
            3 to 7
        }

        if (canExchangeBlorius) {
            3 to 5 eq {
                icon { material { Material.AMETHYST_SHARD } }
                name {
                    localization(
                        player = menu.viewer,
                        tags = {
                            parsedPlaceholder("value", CONF.guild.vitality.bloriusExchangeValue.format(2))
                        }
                    ) {
                        this.guild.menu.menuGuild_bankButtonExchange_vitality
                    }
                }
                description {
                    localization(
                        player = menu.viewer,
                        tags = {
                            parsedPlaceholder("value", CONF.guild.vitality.bloriusExchangePrice.toString())
                        }
                    ) {
                        this.guild.menu.menuGuild_bankButtonExchange_vitalityDescription
                    }
                }
                clickEvent { clickContext ->
                    val dao = DB.getBloriusToVitalityTimes(guild.gid)
                    if (dao != null && dao.times >= CONF.guild.vitality.bloriusExchangeMaxTimes) {
                        clickContext.viewer.send {
                            localization(menu.viewer) {
                                this.guild.guildVitalityExchangeLimit
                            }
                        }
                        return@clickEvent
                    }
                    if (ThirdPartys.playerPoints.look(clickContext.viewer.uniqueId) < CONF.guild.vitality.bloriusExchangePrice) {
                        clickContext.viewer.send {
                            localization(menu.viewer) {
                                this.guild.guildVitalityExchangeNot_enough_money
                            }
                        }
                        return@clickEvent
                    }

                    ThirdPartys.playerPoints.take(
                        clickContext.viewer.uniqueId,
                        CONF.guild.vitality.bloriusExchangePrice
                    )

                    val finalDao = dao ?: DB.trans {
                        GuildBloriusToVitalityTimesDao.new {
                            this.guildId = guild.gid
                            this.times = 0
                        }
                    }
                    DB.trans {
                        guild.vitality += CONF.guild.vitality.bloriusExchangeValue
                        guild.flush()

                        finalDao.times += 1
                        finalDao.flush()
                    }

                    clickContext.viewer.send {
                        localization(
                            player = menu.viewer,
                            tags = {
                                parsedPlaceholder("value", CONF.guild.vitality.bloriusExchangeValue.format(2))
                            }
                        ) {
                            this.guild.guildVitalityExchangeSuccess
                        }
                    }
                }
            }
        }

        storeButtonPosition eq {
            icon { material { Material.CHEST } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuGuild_bankButtonStore
                }
            }
            description {
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("amount", ThirdPartys.vaultApi.getBalance(menu.viewer).format(2))
                    }
                ) {
                    this.guild.menu.menuGuild_bankButtonStoreDescription
                }
            }
            clickEvent { clickContext ->
                if (!(ThirdPartys.vaultApi.getBalance(clickContext.viewer) > 0)) {
                    clickContext.viewer.send {
                        localization(clickContext.viewer) {
                            this.guild.menu.menuGuild_bankButtonStoreWarning
                        }
                    }
                    return@clickEvent
                }
                if (!DB.getRolePermissions(clickContext.viewer.uniqueId, guild.gid).storeBank) {
                    clickContext.viewer.send {
                        localization(clickContext.viewer) {
                            this.guild.guildBankStoreNo_permission
                        }
                    }
                    return@clickEvent
                }
                clickContext.viewer.openDialog(
                    guildBank_storeDialog(clickContext.viewer, guild, { clickContext.menu.rerender() })
                )
            }
        }

        withdrawButtonPosition eq {
            icon { material { Material.ENDER_CHEST } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuGuild_bankButtonWithdraw
                }
            }
            description {
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("amount", guild.bankBalance.format(2))
                    }
                ) {
                    this.guild.menu.menuGuild_bankButtonWithdrawDescription
                }
            }
            clickEvent { clickContext ->
                if (!(guild.bankBalance > 0)) {
                    clickContext.viewer.send {
                        localization(clickContext.viewer) {
                            this.guild.menu.menuGuild_bankButtonWithdrawWarning
                        }
                    }
                    return@clickEvent
                }
                if (!DB.getRolePermissions(clickContext.viewer.uniqueId, guild.gid).withdrawBank) {
                    clickContext.viewer.send {
                        localization(clickContext.viewer) {
                            this.guild.guildBankWithdrawNo_permission
                        }
                    }
                    return@clickEvent
                }
                clickContext.viewer.openDialog(
                    guildBank_withdrawDialog(clickContext.viewer, guild, { clickContext.menu.rerender() })
                )
            }
        }
    }
}