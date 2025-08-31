package blora.command.defaults

import blora.extension.format
import blora.extension.localization
import blora.extension.openDialog
import blora.internal.api.command.BloraCommandLib
import blora.internal.api.command.executor
import blora.internal.api.command.literal
import blora.internal.api.command.playerExecutor
import blora.internal.api.command.requires
import blora.localization.i18n
import blora.permission.Permissions
import blora.plugin.ThirdPartys
import blora.redeem.menu.redeemManagementMenu
import blora.redeem.redeemDialog
import gg.auroramc.aurora.api.AuroraAPI
import gg.auroramc.aurora.api.user.AuroraUser
import gg.auroramc.levels.api.AuroraLevelsProvider
import gg.auroramc.levels.api.data.LevelData
import kotlinx.coroutines.future.await
import me.yic.xconomy.api.XConomyAPI
import me.yic.xconomy.data.caches.Cache
import org.black_ixx.playerpoints.PlayerPoints
import org.black_ixx.playerpoints.manager.DataManager
import org.bukkit.Bukkit
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import java.util.UUID

object LeadCommand {

    fun register() {
        BloraCommandLib.registerCommand("lead") {
            requires {
                return@requires this.hasPermission(Permissions.Commands.Lead)
                        || this.hasPermission(Permissions.Admin)
            }

            literal("coins") {
                requires {
                    this.hasPermission(Permissions.Commands.LeadCoins)
                }

                executor {
                    val player = if (this.isPlayer) this.player.asBukkit else null
                    this.invoker.sendMessage {
                        if (i18n(player) { this.leaderboard.coinsPrefix }.ifBlank { "" }.isNotEmpty()) {
                            localization(player) {
                                this.leaderboard.coinsPrefix
                            }
                        }
                        val xconomyApi =XConomyAPI()
                        xconomyApi.getbalancetop()
                            .forEachIndexed { index, playerName ->
                                newline()
                                localization(
                                    player,
                                    tags = {
                                        parsedPlaceholder("rank", index.toString())
                                        parsedPlaceholder("player", playerName)
                                        parsedPlaceholder(
                                            "coins",
                                            xconomyApi.getPlayerData(playerName)?.balance?.toString() ?: "-1"
                                        )
                                    }
                                ) {
                                    this.leaderboard.coinsContent
                                }
                            }
                        if (i18n(player) { this.leaderboard.coinsSuffix }.ifBlank { "" }.isNotEmpty()) {
                            newline()
                            localization(player) {
                                this.leaderboard.coinsSuffix
                            }
                        }
                    }
                }
            }

            literal("blorius") {
                requires {
                    this.hasPermission(Permissions.Commands.LeadBlroius)
                }

                executor {
                    val player = if (this.isPlayer) this.player.asBukkit else null
                    this.invoker.sendMessage {
                        if (i18n(player) { this.leaderboard.bloriusPrefix }.ifBlank { "" }.isNotEmpty()) {
                            localization(player) {
                                this.leaderboard.bloriusPrefix
                            }
                        }
                        PlayerPoints.getInstance()
                            .getManager(DataManager::class.java)
                            .getTopSortedPoints(10)
                            .forEachIndexed { index, leaderboardEntry ->
                                newline()
                                localization(
                                    player,
                                    tags = {
                                        parsedPlaceholder("rank", index.toString())
                                        parsedPlaceholder("player", leaderboardEntry.username)
                                        parsedPlaceholder("blorius", leaderboardEntry.points.toString())
                                    }
                                ) {
                                    this.leaderboard.bloriusContent
                                }
                            }
                        if (i18n(player) { this.leaderboard.bloriusSuffix }.ifBlank { "" }.isNotEmpty()) {
                            newline()
                            localization(player) {
                                this.leaderboard.bloriusSuffix
                            }
                        }
                    }
                }
            }

            literal("levels") {
                requires {
                    this.hasPermission(Permissions.Commands.LeadLevels)
                }

                executor {
                    suspend fun getAuroraUser(uuid: UUID): AuroraUser {
                        val player = Bukkit.getPlayer(uuid)
                        return if (player != null && player.isOnline) {
                            AuroraAPI.getUser(uuid)
                        } else {
                            AuroraAPI.getUserManager().loadUserFromStorage(uuid)
                                .await()
                        }
                    }
                    val player = if (this.isPlayer) this.player.asBukkit else null
                    val leaderboardData = AuroraAPI.getLeaderboards()
                        .getBoard("levels")
                        .sortedBy { it.position }
                        .take(10)
                        .associateWith { getAuroraUser(it.uuid).getData(LevelData::class.java) }
                    this.invoker.sendMessage {
                        if (i18n(player) { this.leaderboard.levelsPrefix }.ifBlank { "" }.isNotEmpty()) {
                            localization(player) {
                                this.leaderboard.levelsPrefix
                            }
                        }
                        for (leaderboardEntry in leaderboardData) {
                            newline()
                            localization(
                                player,
                                tags = {
                                    parsedPlaceholder("rank", leaderboardEntry.key.position.toString())
                                    parsedPlaceholder("player", leaderboardEntry.key.name)
                                    parsedPlaceholder("level", leaderboardEntry.value.level.toString())
                                    parsedPlaceholder("exp", leaderboardEntry.value.currentXP.format(2))
                                }
                            ) {
                                this.leaderboard.levelsContent
                            }
                        }
                        if (i18n(player) { this.leaderboard.levelsSuffix }.ifBlank { "" }.isNotEmpty()) {
                            newline()
                            localization(player) {
                                this.leaderboard.levelsSuffix
                            }
                        }
                    }
                }
            }
        }
    }

}