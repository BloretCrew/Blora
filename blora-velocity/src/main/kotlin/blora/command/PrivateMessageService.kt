package blora.command

import blora.BloraPlugin
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.unparsedPlaceholder
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Shared private-message send path for /tell and /reply.
 * Tracks the last player who messaged each user for /reply.
 */
object PrivateMessageService {

    /** receiver UUID -> last sender UUID who private-messaged them */
    private val lastMessager: ConcurrentHashMap<UUID, UUID> = ConcurrentHashMap()

    fun getLastMessager(player: Player): UUID? {
        return lastMessager[player.uniqueId]
    }

    fun clear(player: Player) {
        lastMessager.remove(player.uniqueId)
        // Drop reverse entries pointing at this player (optional cleanliness).
        lastMessager.entries.removeIf { it.value == player.uniqueId }
    }

    fun send(sender: Player, target: Player, message: String) {
        val chatConfig = BloraPlugin.configuration.chat

        sender.send {
            mini(
                chatConfig.privateMessageSendFormat
                    .replace("<sender>", sender.username)
                    .replace("<receiver>", target.username)
            ) {
                // Message body is plain text — no MiniMessage injection from players.
                unparsedPlaceholder("message", message)
            }
        }
        target.send {
            mini(
                chatConfig.privateMessageReceiveFormat
                    .replace("<sender>", sender.username)
                    .replace("<receiver>", target.username)
            ) {
                unparsedPlaceholder("message", message)
            }
        }

        // Target can /reply to this sender.
        lastMessager[target.uniqueId] = sender.uniqueId

        val spyText = component {
            mini(
                chatConfig.privateMessageSpyFormat
                    .replace("<sender>", sender.username)
                    .replace("<receiver>", target.username)
            ) {
                unparsedPlaceholder("message", message)
            }
        }
        BloraPlugin.proxyServer.allPlayers
            .filter { online ->
                online != sender &&
                    online != target &&
                    online.hasPermission(SpyCommand.PERMISSION) &&
                    PrivateMessageSpy.isEnabled(online)
            }
            .forEach { online ->
                online.sendMessage(spyText)
            }
        BloraPlugin.proxyServer.consoleCommandSource.sendMessage(spyText)

        PrivateMessageLogger.log(sender.username, target.username, message)
    }

}
