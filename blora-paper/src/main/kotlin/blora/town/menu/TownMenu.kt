package blora.town.menu

import blora.configuration.CONF
import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.town.dao.TownChunkDao
import blora.database.town.dao.TownDao
import blora.extension.*
import blora.item.clone
import blora.item.material
import blora.menu.line5_confirmrationMenu
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.*
import blora.town.dataprovider.GuildTownDaoDataProvider
import blora.town.dialog.createTownDialog
import blora.util.castString
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun townMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id
    return pageableMenuPage(menu, GuildTownDaoDataProvider(guild)) {
        val permissions = DB.getRolePermissions(menu.viewer.uniqueId, guild.gid)

        pageId {
            "guild_${guildId}_towns"
        }

        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.town.menu.townsTitle
            }
        }

        showBackButton()

        if (permissions.townManagement) {
            5 to 5 eq {
                icon { material { Material.OAK_SAPLING } }
                name {
                    localization(menu.viewer) {
                        this.town.menu.townsButtonCreateTown
                    }
                }
                clickEvent { clickContext ->
                    DB.trans {
                        guild.refresh()
                    }
                    val permissions = DB.getRolePermissions(clickContext.viewer.uniqueId, guild.gid)
                    if (!permissions.townManagement)
                        return@clickEvent
                    if (CONF.town.firstChunkPrice <= 0)
                        return@clickEvent
                    if (TownDao.list(guild.gid).size >= CONF.town.townsPerGuild) {
                        clickContext.viewer.send {
                            localization(
                                player = clickContext.viewer,
                                tags = {
                                    parsedPlaceholder("limit", CONF.town.townsPerGuild.toString())
                                }
                            ) {
                                this.town.createWarningTowns_limit
                            }
                        }
                        return@clickEvent
                    }
                    if (TownChunkDao.listByGuild(guild.gid).size >= guild.maxClaims) {
                        clickContext.viewer.send {
                            localization(
                                player = clickContext.viewer,
                                tags = {
                                    parsedPlaceholder("limit", guild.maxClaims.toString())
                                }
                            ) {
                                this.town.createWarningChunks_limit
                            }
                        }
                        return@clickEvent
                    }
                    if (CONF.town.firstChunkPrice > guild.bankBalance) {
                        clickContext.viewer.send {
                            localization(
                                player = clickContext.viewer,
                                tags = {
                                    parsedPlaceholder("cost", CONF.town.firstChunkPrice.format(2))
                                }
                            ) {
                                this.town.createWarningBank_balance_not_enough
                            }
                        }
                        return@clickEvent
                    }
                    val chunk = clickContext.viewer.chunk
                    if (chunk.isClaimedByAnyTown()) {
                        clickContext.viewer.send {
                            localization(clickContext.viewer) {
                                this.town.createWarningChunks_occupied
                            }
                        }
                        return@clickEvent
                    }
                    clickContext.viewer.openDialog(
                        createTownDialog(clickContext.viewer, guild, {
                            clickContext.menu.rerender()
                        })
                    )
                }
            }
        }

        dataItem { viewContext, town, dataIndex ->
            icon { clone { town.parsedIcon } }
            name {
                mini(town.displayName)
            }
            description {
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("chunks", TownChunkDao.listByTown(town.townId).size.toString())
                    }
                ) {
                    this.town.menu.townsItemDescriptionChunks
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("creator", DB.getPlayerDisplayName(town.creator))
                    }
                ) {
                    this.town.menu.townsItemDescriptionCreator
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("date", town.createdAt.castString())
                    }
                ) {
                    this.town.menu.townsItemDescriptionCreatedAt
                }
                if (permissions.townManagement) {
                    newline()
                    localization(player = menu.viewer) {
                        this.town.menu.townsItemDescriptionRight
                    }
                }
            }
            clickEvent { clickContext ->
                DB.trans {
                    guild.refresh()
                }
                val permissions = DB.getRolePermissions(clickContext.viewer.uniqueId, guild.gid)
                if (!permissions.townManagement)
                    return@clickEvent
                if (clickContext.click.isLeftClick) {
                    clickContext.stack.push {
                        townManagementMenu(menu, guild, town)
                    }
                } else if (clickContext.click.isRightClick) {
                    line5_confirmrationMenu(
                        menu, component {
                            localization(
                                player = menu.viewer,
                                tags = {
                                    parsedPlaceholder("town", town.displayName)
                                }
                            ) {
                                this.town.menu.confirmationDelete_townTitle
                            }
                        }
                    ) {
                        val townChunks = town.listChunks()
                        DB.trans {
                            for (townChunk in townChunks) {
                                val price =
                                    if (townChunk.chunkX == town.centerChunkX && townChunk.chunkZ == town.centerChunkZ) {
                                        CONF.town.giveBackFirstChunk
                                    } else {
                                        CONF.town.giveBackPerChunk
                                    }
                                guild.bankBalance += price
                                townChunk.delete()
                            }
                            town.delete()
                            if (guild.bankBalance > guild.bankBalanceMax)
                                guild.bankBalanceMax = guild.bankBalance
                            guild.flush()
                        }
                        clickContext.viewer.send {
                            localization(
                                player = clickContext.viewer,
                                tags = {
                                    parsedPlaceholder("town_name", town.displayName)
                                }
                            ) {
                                this.town.deleteSuccess
                            }
                        }
                    }
                }
            }
        }
    }
}