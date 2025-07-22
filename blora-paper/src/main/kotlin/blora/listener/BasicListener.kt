package blora.listener

import blora.database.DB
import blora.database.player.dao.PlayerInfoDao
import blora.database.player.table.PlayerInfoTable
import blora.plugin.BloraPlugin
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object BasicListener : Listener {

    fun register() {
        Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
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

}