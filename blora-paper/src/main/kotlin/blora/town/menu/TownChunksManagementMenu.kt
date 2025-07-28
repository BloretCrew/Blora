@file:Suppress("UnstableApiUsage")

package blora.town.menu

import blora.configuration.CONF
import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.town.dao.TownChunkDao
import blora.database.town.dao.TownDao
import blora.extension.*
import blora.item.itemFlag
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.*
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun townChunksManagementMenu(menu: Menu, guild: GuildDao, town: TownDao): MenuPage<*, *> {
    val townId = town.id
    val world = Bukkit.getWorld(town.world)!!
    var centerChunkX = town.centerChunkX
    var centerChunkZ = town.centerChunkZ
    return completeDynamicMenuPage(menu) {
        pageId {
            "townManagement_${townId}_chunksManagement"
        }

        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("town", town.displayName)
                }
            ) {
                this.town.menu.town_managementTown_chunks_managementTitle
            }
        }

        mapping(
            "#       #",
            "#       #",
            "#       #",
            "#       #",
            "#       #"
        )

        '#' eq {
            icon { material { Material.BLACK_STAINED_GLASS_PANE } }
        }

        backButton()

        2 to 1 eq {
            icon { material { Material.FEATHER } }
            name {
                localization(menu.viewer) {
                    this.town.menu.town_managementTown_chunks_managementButtonUp
                }
            }
            clickEvent { clickContext ->
                centerChunkZ += 1
                clickContext.menu.rerender()
            }
        }
        3 to 1 eq {
            icon { material { Material.DISC_FRAGMENT_5 } }
            name {
                localization(menu.viewer) {
                    this.town.menu.town_managementTown_chunks_managementButtonDown
                }
            }
            clickEvent { clickContext ->
                centerChunkZ -= 1
                clickContext.menu.rerender()
            }
        }
        4 to 1 eq {
            icon { material { Material.INK_SAC } }
            name {
                localization(menu.viewer) {
                    this.town.menu.town_managementTown_chunks_managementButtonLeft
                }
            }
            clickEvent { clickContext ->
                centerChunkX -= 1
                clickContext.menu.rerender()
            }
        }
        5 to 1 eq {
            icon { material { Material.IRON_NUGGET } }
            name {
                localization(menu.viewer) {
                    this.town.menu.town_managementTown_chunks_managementButtonRight
                }
            }
            clickEvent { clickContext ->
                centerChunkX += 1
                clickContext.menu.rerender()
            }
        }

        for (xDelta in 7 downTo 1) {
            for (zDelta in 5 downTo 1) {
                val requestChunkX = centerChunkX + xDelta - 4
                val requestChunkZ = centerChunkZ + 3 - zDelta
                val townChunk = TownChunkDao.find(world.name, requestChunkX, requestChunkZ)
                val chunk = world.getChunkAt(requestChunkX, requestChunkZ)
                val minPoint = chunk.smallestPoint()
                val maxPoint = chunk.biggestPoint()
                zDelta to (xDelta + 1) eq {
                    icon {
                        if (townChunk == null) {
                            if (chunk.containsPlayerResidence()) {
                                material { Material.GRAY_STAINED_GLASS_PANE } // some player has residence in this chunk
                            } else {
                                material { Material.LIGHT_BLUE_STAINED_GLASS_PANE } // claimable
                            }
                        } else {
                            if (townChunk.townId == town.townId) {
                                material { Material.LIME_STAINED_GLASS_PANE } // claimed
                                if (townChunk.chunkX == town.centerChunkX && townChunk.chunkZ == town.centerChunkZ) {
                                    DataComponentTypes.ENCHANTMENTS eq ItemEnchantments.itemEnchantments(
                                        mapOf(Enchantment.PROTECTION to 1)
                                    )
                                    itemFlag {
                                        ItemFlag.HIDE_ENCHANTS
                                    }
                                }
                            } else {
                                if (townChunk.guildId == town.guildId) {
                                    material { Material.YELLOW_STAINED_GLASS_PANE } // claimed by other towns in same guild
                                } else {
                                    material { Material.RED_STAINED_GLASS_PANE } // claimed by other guild
                                }
                            }
                        }
                    }
                    name {
                        localization(
                            menu.viewer,
                            tags = {
                                parsedPlaceholder("chunk_x", chunk.x.toString())
                                parsedPlaceholder("chunk_z", chunk.z.toString())
                            }
                        ) {
                            this.town.menu.town_managementTown_chunks_managementChunkName
                        }
                    }
                    description {
                        newline()
                        localization(
                            menu.viewer,
                            tags = {
                                parsedPlaceholder("min_x", minPoint.x.format(2))
                                parsedPlaceholder("min_z", minPoint.z.format(2))
                                parsedPlaceholder("max_x", maxPoint.x.format(2))
                                parsedPlaceholder("max_z", maxPoint.z.format(2))
                            }
                        ) {
                            this.town.menu.town_managementTown_chunks_managementChunkDescriptionLine1
                        }
                        newline()
                        localization(
                            menu.viewer,
                            tags = {
                                parsedPlaceholder("min_x", minPoint.x.format(2))
                                parsedPlaceholder("min_z", minPoint.z.format(2))
                                parsedPlaceholder("max_x", maxPoint.x.format(2))
                                parsedPlaceholder("max_z", maxPoint.z.format(2))
                            }
                        ) {
                            this.town.menu.town_managementTown_chunks_managementChunkDescriptionLine2
                        }
                        newline()
                        newline()
                        localization(menu.viewer) {
                            if (townChunk == null) {
                                if (chunk.containsPlayerResidence()) {
                                    this.town.menu.town_managementTown_chunks_managementChunkDescriptionHas_player_residence
                                } else {
                                    this.town.menu.town_managementTown_chunks_managementChunkDescriptionClaimable
                                }
                            } else {
                                if (townChunk.townId == town.townId) {
                                    if (townChunk.chunkX == town.centerChunkX && townChunk.chunkZ == town.centerChunkZ) {
                                        this.town.menu.town_managementTown_chunks_managementChunkDescriptionCenter
                                    } else {
                                        this.town.menu.town_managementTown_chunks_managementChunkDescriptionClaimed
                                    }
                                } else {
                                    if (townChunk.guildId == town.guildId) {
                                        this.town.menu.town_managementTown_chunks_managementChunkDescriptionClaimed_by_same_guild
                                    } else {
                                        this.town.menu.town_managementTown_chunks_managementChunkDescriptionClaimed_by_other
                                    }
                                }
                            }
                        }
                        if (townChunk != null && townChunk.townId == town.townId &&
                            (townChunk.chunkX != town.centerChunkX || townChunk.chunkZ != town.centerChunkZ)
                        ) {
                            newline()
                            localization(menu.viewer) {
                                this.town.menu.town_managementTown_chunks_managementChunkDescriptionRight_click
                            }
                        }
                    }
                    clickEvent { clickContext ->
                        val townChunk = TownChunkDao.find(world.name, requestChunkX, requestChunkZ)
                        if (clickContext.click.isLeftClick) {
                            if (townChunk == null && !chunk.containsPlayerResidence()) {
                                if (chunk.getSurroundingTownChunks().any { it.townId == town.townId }) {
                                    DB.trans {
                                        guild.refresh()
                                    }
                                    val permissions = DB.getRolePermissions(clickContext.viewer.uniqueId, guild.gid)
                                    if (!permissions.townManagement) {
                                        clickContext.menu.rerender()
                                        return@clickEvent
                                    }
                                    if (CONF.town.firstChunkPrice <= 0) {
                                        clickContext.menu.rerender()
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
                                                this.town.claimWarningChunks_limit
                                            }
                                        }
                                        clickContext.menu.rerender()
                                        return@clickEvent
                                    }
                                    if (CONF.town.pricePerChunk > guild.bankBalance) {
                                        clickContext.viewer.send {
                                            localization(
                                                player = clickContext.viewer,
                                                tags = {
                                                    parsedPlaceholder("cost", CONF.town.pricePerChunk.format(2))
                                                }
                                            ) {
                                                this.town.claimWarningBank_balance_not_enough
                                            }
                                        }
                                        clickContext.menu.rerender()
                                        return@clickEvent
                                    }

                                    DB.trans {
                                        TownChunkDao.new {
                                            this.townId = town.townId
                                            this.guildId = guild.gid

                                            this.world = town.world
                                            this.chunkX = requestChunkX
                                            this.chunkZ = requestChunkZ
                                        }

                                        guild.bankBalance -= CONF.town.pricePerChunk
                                        guild.flush()
                                    }
                                    clickContext.viewer.send {
                                        localization(clickContext.viewer) {
                                            this.town.claimSuccess
                                        }
                                    }
                                } else {
                                    clickContext.viewer.send {
                                        localization(clickContext.viewer) {
                                            this.town.claimWarningMust_beside_claimed
                                        }
                                    }
                                }
                            } else {
                                clickContext.viewer.send {
                                    localization(clickContext.viewer) {
                                        this.town.claimWarningClaimed
                                    }
                                }
                            }
                            clickContext.menu.rerender()
                        } else if (clickContext.click.isRightClick) {
                            if (townChunk != null && townChunk.townId == town.townId
                                && (townChunk.chunkX != town.centerChunkX || townChunk.chunkZ != town.centerChunkZ)
                            ) {
                                if (chunk.getSurroundingTownChunks().any {
                                        return@any if (it.chunkX == town.centerChunkX && it.chunkZ == town.centerChunkZ)
                                            false
                                        else {
                                            !(it.townId == townChunk.townId && it.checkHavePassToCenter(
                                                town.centerChunkX,
                                                town.centerChunkZ,
                                                townChunk.chunkX,
                                                townChunk.chunkZ
                                            ))
                                        }
                                    }) {
                                    clickContext.viewer.send {
                                        localization(clickContext.viewer) {
                                            this.town.remove_claimWarningSeparate_if_remove
                                        }
                                    }
                                    return@clickEvent
                                }
                                DB.trans {
                                    guild.refresh()
                                    townChunk.delete()
                                    guild.bankBalance += CONF.town.giveBackPerChunk
                                    if (guild.bankBalance > guild.bankBalanceMax)
                                        guild.bankBalanceMax = guild.bankBalance
                                    guild.flush()
                                }
                                clickContext.viewer.send {
                                    localization(clickContext.viewer) {
                                        this.town.remove_claimSuccess
                                    }
                                }
                                clickContext.menu.rerender()
                            }
                        }
                    }
                }
            }
        }
    }
}