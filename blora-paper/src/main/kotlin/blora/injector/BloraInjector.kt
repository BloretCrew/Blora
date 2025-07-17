@file:Suppress("UNCHECKED_CAST")
@file:OptIn(ExperimentalAtomicApi::class)

package blora.injector

import blora.listener.packet.CustomClickActionHandler
import blora.plugin.BloraPlugin
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.Connection
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket
import net.minecraft.server.network.ServerConnectionListener
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.server.PluginDisableEvent
import java.util.*
import kotlin.concurrent.Volatile
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi


object BloraInjector {

    const val identifier = "blora_injector"

    internal val serverConnection: ServerConnectionListener
    internal val networkManagers: List<Connection>
    internal val pendingNetworkManagers: Queue<Connection>

    internal val playerCache: MutableMap<UUID, Player> = Collections.synchronizedMap(HashMap())
    internal val injectedChannels: MutableSet<Channel> =
        Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap()))

    private val closed: AtomicBoolean = AtomicBoolean(false)

    init {
        if (!Bukkit.isPrimaryThread()) {
            throw IllegalStateException("Injector must be in a main thread!")
        }
        this.serverConnection = (Bukkit.getServer() as CraftServer).server.connection
        this.networkManagers = this.serverConnection.connections
        this.pendingNetworkManagers = ServerConnectionListener::class
            .java
            .getDeclaredField("pending")
            .apply {
                this.isAccessible = true
            }
            .get(this.serverConnection) as Queue<Connection>
    }

    fun init() {
        Bukkit.getPluginManager().registerEvents(InjectorListener, BloraPlugin)

        for (player in Bukkit.getOnlinePlayers()) {
            injectPlayer(player)
        }
    }

    fun onPacketReceiveAsync(sender: Player?, channel: Channel, packet: Any): Any? {
        if (packet is ServerboundCustomClickActionPacket) {
            CustomClickActionHandler.handle(packet)
        }
        return packet
    }

    fun onPacketSendAsync(sender: Player?, channel: Channel, packet: Any): Any? {
        return packet
    }

    fun close() {
        if (closed.load()) {
            return
        }
        closed.store(true)

        InjectorListener.unregister()

        synchronized(networkManagers) {
            for (manager in networkManagers) {
                val channel: Channel = manager.channel
                channel.eventLoop().submit({ channel.pipeline().remove(identifier) })
            }
        }

        playerCache.clear()
        injectedChannels.clear()
    }


    fun isClosed(): Boolean {
        return this.closed.load()
    }

    internal fun injectPlayer(player: Player) {
        injectChannel((player as CraftPlayer).handle.connection.connection.channel).player = player
    }

    internal fun injectNetworkManager(connection: Connection) {
        val channel = connection.channel
        if (!this.injectedChannels.contains(channel)) {
            this.injectChannel(channel)
        }
    }

    internal fun injectChannel(channel: Channel): BloraPacketHandler {
        val handler = BloraPacketHandler()

        channel.eventLoop().submit {
            if (isClosed())
                return@submit
            if (injectedChannels.add(channel)) {
                channel.pipeline().addBefore("packet_handler", identifier, handler)
            }
        }

        return handler
    }

}

internal object InjectorListener : Listener {

    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (BloraInjector.isClosed())
            return
        synchronized(BloraInjector.networkManagers) {
            if (BloraInjector.networkManagers is RandomAccess) {
                for (i in BloraInjector.networkManagers.size - 1 downTo 0) {
                    BloraInjector.injectNetworkManager(BloraInjector.networkManagers[i])
                }
            } else {
                for (networkManager in BloraInjector.networkManagers) {
                    BloraInjector.injectNetworkManager(networkManager)
                }
            }
            synchronized(BloraInjector.pendingNetworkManagers) {
                for (networkManager in BloraInjector.pendingNetworkManagers) {
                    BloraInjector.injectNetworkManager(networkManager)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerLoginEvent(event: PlayerLoginEvent) {
        if (BloraInjector.isClosed()) {
            return
        }

        BloraInjector.playerCache[event.player.uniqueId] = event.player
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if (BloraInjector.isClosed()) {
            return
        }
        val player = event.player

        val networkManager = (event.player as CraftPlayer).handle.connection.connection
        val channel = networkManager.channel
        val channelHandler = channel.pipeline().get(BloraInjector.identifier)
        if (channelHandler != null) {
            if (channelHandler is BloraPacketHandler) {
                channelHandler.player = player
                BloraInjector.playerCache.remove(player.uniqueId)
            }
            return
        }

        BloraInjector.injectChannel(channel).player = player
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPluginDisableEvent(event: PluginDisableEvent) {
        if (event.plugin is BloraPlugin)
            BloraInjector.close()
    }

    internal fun unregister() {
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(this)
        PlayerLoginEvent.getHandlerList().unregister(this)
        PlayerJoinEvent.getHandlerList().unregister(this)
        PluginDisableEvent.getHandlerList().unregister(this)
    }

}

internal class BloraPacketHandler : ChannelDuplexHandler() {

    @Volatile
    internal var player: Player? = null

    @Throws(Exception::class)
    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        BloraInjector.injectedChannels.remove(ctx.channel())
        super.channelUnregistered(ctx)
    }

    @Throws(Exception::class)
    override fun write(ctx: ChannelHandlerContext, packet: Any, promise: ChannelPromise) {
        if (player == null && packet is ClientboundLoginFinishedPacket) {
            val player: Player? = BloraInjector.playerCache.remove(packet.gameProfile.id)

            if (player != null) {
                this.player = player
            }
        }


        val newPacket: Any? = BloraInjector.onPacketSendAsync(player, ctx.channel(), packet)
        if (newPacket != null)
            super.write(ctx, newPacket, promise)
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
        val newPacket: Any? = BloraInjector.onPacketReceiveAsync(player, ctx.channel(), packet)
        if (newPacket != null)
            super.channelRead(ctx, newPacket)
    }
}