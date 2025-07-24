package blora.listener

import blora.configuration.CONF
import blora.database.DB
import blora.extension.localization
import blora.plugin.BloraPlugin
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder
import kotlin.math.max

object GuildListener : Listener {

    private var registered: Boolean = false

    fun register() {
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
            registered = true
        }
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
        this.registered = false
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        Bukkit.getScheduler().runTaskLater(
            BloraPlugin,
            Runnable {
                if (!event.player.isOnline)
                    return@Runnable
                val player = event.player
                for (joinNotify in DB.listJoinNotifies(player.uniqueId)) {
                    DB.trans {
                        joinNotify.delete()
                    }
                    player.send {
                        if (joinNotify.result) {
                            localization(
                                player = player,
                                tags = {
                                    parsedPlaceholder("guild", joinNotify.guildName)
                                }
                            ) {
                                this.guild.guildJoin_requestAccepted
                            }
                        } else {
                            localization(
                                player = player,
                                tags = {
                                    parsedPlaceholder("guild", joinNotify.guildName)
                                }
                            ) {
                                this.guild.guildJoin_requestRejected
                            }
                        }
                    }
                }
                for (kickNotify in DB.listKickNotifies(player.uniqueId)) {
                    DB.trans {
                        kickNotify.delete()
                    }
                    player.send {
                        localization(
                            player = player,
                            tags = {
                                parsedPlaceholder("guild", kickNotify.guildName)
                                parsedPlaceholder("player", DB.getPlayerDisplayName(kickNotify.operator))
                            }
                        ) {
                            this.guild.guildKick
                        }
                    }
                }
                for (disbandNotify in DB.listDisbandNotifies(player.uniqueId)) {
                    DB.trans {
                        if (disbandNotify.members.size <= 1) {
                            disbandNotify.delete()
                        } else {
                            disbandNotify.members = disbandNotify.members
                                .toMutableList()
                                .apply { remove(player.uniqueId) }
                                .toList()
                            disbandNotify.flush()
                        }
                    }
                    player.send {
                        localization(
                            player = player,
                            tags = {
                                parsedPlaceholder("guild_id", disbandNotify.guildId)
                                parsedPlaceholder("guild_name", disbandNotify.guildName)
                            }
                        ) {
                            this.guild.guildDisbandNotify
                        }
                    }
                }
            },
            max(CONF.mail.notifyDelaySeconds, 0) * 20L
        )
    }

}