package blora.command

import blora.BloraPlugin
import blora.database.player.PlayerDao
import blora.database.player.PlayerTable
import com.velocitypowered.api.proxy.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Per-player private-message spy toggle.
 * Default is off; state is persisted in [blora.options.PlayerOptions.privateMessageSpy].
 */
object PrivateMessageSpy {

    private val enabledPlayers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val loadedPlayers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    fun isEnabled(player: Player): Boolean {
        ensureLoaded(player)
        return enabledPlayers.contains(player.uniqueId)
    }

    /**
     * @return `true` if spy is now enabled, `false` if now disabled
     */
    fun toggle(player: Player): Boolean {
        ensureLoaded(player)
        val nowEnabled = if (enabledPlayers.remove(player.uniqueId)) {
            false
        } else {
            enabledPlayers.add(player.uniqueId)
            true
        }
        persist(player, nowEnabled)
        return nowEnabled
    }

    /**
     * Load persisted spy flag into the in-memory cache (e.g. on login).
     */
    fun load(player: Player) {
        val enabled = readPersisted(player)
        if (enabled) {
            enabledPlayers.add(player.uniqueId)
        } else {
            enabledPlayers.remove(player.uniqueId)
        }
        loadedPlayers.add(player.uniqueId)
    }

    fun clear(player: Player) {
        enabledPlayers.remove(player.uniqueId)
        loadedPlayers.remove(player.uniqueId)
    }

    fun clear(uuid: UUID) {
        enabledPlayers.remove(uuid)
        loadedPlayers.remove(uuid)
    }

    private fun ensureLoaded(player: Player) {
        if (!loadedPlayers.contains(player.uniqueId)) {
            load(player)
        }
    }

    private fun readPersisted(player: Player): Boolean {
        return try {
            BloraPlugin.database.trans {
                findPlayerDao(player)?.jsonOptions?.privateMessageSpy == true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun persist(player: Player, enabled: Boolean) {
        try {
            BloraPlugin.database.trans {
                val dao = findPlayerDao(player) ?: return@trans
                val options = dao.jsonOptions
                options.privateMessageSpy = enabled
                dao.jsonOptions = options
                dao.flush()
            }
        } catch (ex: Exception) {
            BloraPlugin.log.warn(
                "Failed to persist private-message spy state for ${player.username}: ${ex.message}"
            )
        }
    }

    private fun findPlayerDao(player: Player): PlayerDao? {
        val byUuid = PlayerDao.find {
            PlayerTable.uuid eq player.uniqueId
        }.firstOrNull()
        if (byUuid != null) {
            return byUuid
        }
        return PlayerDao.find {
            PlayerTable.username eq player.username.lowercase()
        }.firstOrNull()
    }

}
