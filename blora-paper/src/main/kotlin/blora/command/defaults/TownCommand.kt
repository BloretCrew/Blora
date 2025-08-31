package blora.command.defaults

import blora.configuration.CONF
import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.town.dao.TownChunkDao
import blora.database.town.dao.TownDao
import blora.extension.format
import blora.extension.getSurroundingTownChunks
import blora.extension.localization
import blora.guild.menu.guildMenu
import blora.internal.api.command.*
import blora.internal.api.command.argument.Arguments
import blora.menu.v2.Menu
import blora.permission.Permissions
import blora.town.menu.townManagementMenu
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.space
import plutoproject.adventurekt.text.text
import kotlin.math.max

object TownCommand {

    fun register() {
        BloraCommandLib.registerCommand("town") {
            requires {
                this.hasPermission(Permissions.Commands.Town)
            }

            playerExecutor {
                guildMenu(this.player.asBukkit).open()
            }

            literal("manage") {
                withManageableGuildAndTown { guild, town ->
                    Menu(
                        this.player.asBukkit,
                        5,
                        200L
                    ).apply {
                        this.closer {
                            it.destroy()
                        }
                        this.stack.push {
                            townManagementMenu(this, guild, town, false)
                        }
                    }.open()
                }
            }

            literal("claim") {
                withManageableGuildAndTown { guild, town ->
                    val player = this.player.asBukkit
                    val chunk = player.chunk
                    val townChunk = TownChunkDao.find(player.world.name, chunk.x, chunk.z)
                    if (townChunk == null) {
                        if (chunk.getSurroundingTownChunks().any { it.townId == town.townId }) {
                            DB.trans {
                                guild.refresh()
                            }
                            val permissions = DB.getRolePermissions(player.uniqueId, guild.gid)
                            if (!permissions.townManagement) {
                                player.send {
                                    localization(
                                        player = player,
                                    ) {
                                        this.town.failedToClaim
                                    }
                                }
                                return@withManageableGuildAndTown
                            }
                            if (CONF.town.firstChunkPrice <= 0) {
                                player.send {
                                    localization(
                                        player = player,
                                    ) {
                                        this.town.failedToClaim
                                    }
                                }
                                return@withManageableGuildAndTown
                            }
                            if (TownChunkDao.listByGuild(guild.gid).size >= guild.maxClaims) {
                                player.send {
                                    localization(
                                        player = player,
                                        tags = {
                                            parsedPlaceholder("limit", guild.maxClaims.toString())
                                        }
                                    ) {
                                        this.town.claimWarningChunks_limit
                                    }
                                }
                                return@withManageableGuildAndTown
                            }
                            if (CONF.town.pricePerChunk > guild.bankBalance) {
                                player.send {
                                    localization(
                                        player = player,
                                        tags = {
                                            parsedPlaceholder("cost", CONF.town.pricePerChunk.format(2))
                                        }
                                    ) {
                                        this.town.claimWarningBank_balance_not_enough
                                    }
                                }
                                return@withManageableGuildAndTown
                            }

                            DB.trans {
                                TownChunkDao.new {
                                    this.townId = town.townId
                                    this.guildId = guild.gid

                                    this.world = town.world
                                    this.chunkX = chunk.x
                                    this.chunkZ = chunk.z
                                }

                                guild.bankBalance -= CONF.town.pricePerChunk
                                guild.flush()
                            }
                            player.send {
                                localization(player) {
                                    this.town.claimSuccess
                                }
                            }
                        } else {
                            player.send {
                                localization(player) {
                                    this.town.claimWarningMust_beside_claimed
                                }
                            }
                        }
                    } else {
                        player.send {
                            localization(player) {
                                this.town.claimWarningClaimed
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun QuickLibCommandBuilder.withManageableGuildAndTown(extraExecutor: suspend CommandContext.(GuildDao, TownDao) -> Unit) {
        argument("town", Arguments.word) { getTownId ->
            suggestsAsync {
                if (this.isPlayer) {
                    TownDao.listManageable(this.player.asBukkit.uniqueId).forEach {
                        this.suggest(it.townId)
                    }
                }
            }
            playerExecutor {
                val town = TownDao.getByTownId(getTownId())
                if (town == null)
                    return@playerExecutor
                val guild = GuildDao.getByGuildId(town.guildId)
                if (guild == null)
                    return@playerExecutor

                if (!DB.getRolePermissions(this.player.asBukkit.uniqueId, guild.gid).townManagement)
                    return@playerExecutor

                this.extraExecutor(guild, town)
            }
        }
    }

}