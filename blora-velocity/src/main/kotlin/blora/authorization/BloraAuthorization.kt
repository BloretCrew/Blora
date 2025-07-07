package blora.authorization

import blora.BloraPlugin
import blora.extension.localization
import blora.messaging.packet.clientbound.PlayerAuthorizationUpdatePacket
import com.github.benmanes.caffeine.cache.Caffeine
import com.velocitypowered.api.proxy.Player
import plutoproject.adventurekt.component
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object BloraAuthorization {

    private val joinAt = mutableMapOf<Player, Long>()
    private val status = mutableMapOf<Player, Boolean>()
    private val playerIps = mutableMapOf<Player, String>()
    private val ips = mutableMapOf<String, MutableList<Player>>()
    private val cachePool = Caffeine.newBuilder()
        .initialCapacity(48)
        .expireAfterWrite(BloraPlugin.configuration.security.autoLoginExpireTime.seconds.toJavaDuration())
        .build<UUID, LoginSession>()
    val passwordRetries = mutableMapOf<Player, Int>()

    fun refreshUnauthorizedPlayers() {
        val removal = mutableListOf<Player>()
        val current = System.currentTimeMillis()
        for ((player, joinAt) in this.joinAt) {
            if (current - joinAt > BloraPlugin.configuration.security.maxNotLogin) {
                removal.add(player)
                player.disconnect(component {
                    localization(player) {
                        this.kickLoginOvertime
                    }
                })
            }
        }
        removal.forEach(this.joinAt::remove)
    }

    fun isAuthorized(player: Player): Boolean {
        return this.status[player] == true
    }

    fun authorize(player: Player) {
        this.status[player] = true
        val packet = PlayerAuthorizationUpdatePacket()
        packet.playerName = player.username
        packet.authorized = true
        BloraPlugin.server.broadcast(packet)
    }

    fun getPlayersUnderIp(ip: String): List<Player> {
        return ips.getOrDefault(ip, listOf()).toList() // use toList to copy a list to prevent modify the original data
    }

    fun join(player: Player) {
        if (!this.status.containsKey(player)) {
            this.status[player] = player.isOnlineMode && BloraPlugin.configuration.security.allowOnlinePlayerAutoLogin
            if (!this.ips.containsKey(player.remoteAddress.address.hostAddress)) {
                this.ips[player.remoteAddress.address.hostAddress] = mutableListOf()
            }
            this.ips[player.remoteAddress.address.hostAddress]!!.add(player)
            this.playerIps[player] = player.remoteAddress.address.hostAddress
        }
        if (BloraPlugin.configuration.security.sameIpAutoLogin) {
            val session = this.cachePool.getIfPresent(player.uniqueId)
            if (session != null) {
                this.cachePool.invalidate(player.uniqueId)
                if (player.remoteAddress.address.hostAddress != session.ip)
                    return
                if (System.currentTimeMillis() - session.quitTime >= BloraPlugin.configuration.security.autoLoginExpireTime)
                    return
                this.status[player] = true
            }
        }
        this.joinAt[player] = System.currentTimeMillis()
    }

    fun clear(player: Player) {
        passwordRetries.remove(player)
        if (this.status.containsKey(player) && this.status[player] == true) {
            // only add player to cache pool when they logged in
            this.cachePool.put(
                player.uniqueId, LoginSession(
                    player.uniqueId,
                    this.playerIps[player]!!,
                    System.currentTimeMillis()
                )
            )
        }
        val status = this.status[player]
        this.status.remove(player)
        val ip = this.playerIps.remove(player)
        if (ip != null) {
            if (this.ips.containsKey(ip)) {
                this.ips[ip]!!.remove(player)
                if (this.ips[ip]!!.isEmpty()) {
                    this.ips.remove(ip)
                }
            }
        }
        if (status == true) {
            player.currentServer.ifPresent {
                val databasePlayer = BloraPlugin.database.getPlayerByName(player.username.lowercase())!!
                BloraPlugin.database.trans {
                    databasePlayer.lastServer = it.server.serverInfo.name
                    databasePlayer.flush()
                }
            }
        }
    }

    fun clearAll() {
        BloraPlugin.proxyServer.allPlayers.forEach(this::clear)
    }

}