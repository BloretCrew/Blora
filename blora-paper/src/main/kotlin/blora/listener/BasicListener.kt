package blora.listener

import blora.database.DB
import blora.database.player.dao.PlayerInfoDao
import blora.database.player.dao.PlayerOnlineDataDao
import blora.database.player.table.PlayerInfoTable
import blora.database.player.table.PlayerOnlineDataTable
import blora.player.PlayerOnlineData
import blora.plugin.BloraPlugin
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

import java.time.LocalDateTime as jtLocalDateTime

object BasicListener : Listener {

    val joinAt: MutableMap<Player, LocalDateTime> = mutableMapOf()

    fun register() {
        Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        joinAt[event.player] = jtLocalDateTime.now().toKotlinLocalDateTime()
        DB.trans {
            val dao = PlayerInfoDao.find {
                PlayerInfoTable.uuid eq event.player.uniqueId
            }.firstOrNull() ?: PlayerInfoDao.new {
                this.playerUuid = event.player.uniqueId
                this.username = event.player.name
            }
            dao.username = event.player.name
            dao.flush()
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val joinAt = this.joinAt[event.player] ?: return
        DB.trans {
            val dao = PlayerOnlineDataDao.find {
                PlayerOnlineDataTable.uuid eq event.player.uniqueId
            }.firstOrNull() ?: PlayerOnlineDataDao.new {
                this.playerUuid = event.player.uniqueId
                this.onlineData = PlayerOnlineData(emptyList())
            }
            dao.onlineData = PlayerOnlineData(
                dao.onlineData
                    .onlineData
                    .toMutableList()
                    .apply {
                        add(joinAt to jtLocalDateTime.now().toKotlinLocalDateTime())
                    }
            )
            dao.flush()
        }
    }

}