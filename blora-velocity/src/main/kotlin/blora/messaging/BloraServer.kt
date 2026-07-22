package blora.messaging

import blora.BloraPlugin
import blora.authorization.BloraAuthorization
import blora.messaging.packet.Packet
import blora.messaging.packet.PacketHandler
import blora.messaging.packet.PacketHandlerManager
import blora.messaging.packet.PacketType
import blora.messaging.packet.clientbound.*
import blora.messaging.packet.common.DebugMessagePacket
import blora.messaging.packet.common.ReloadConfigurationPacket
import blora.messaging.packet.serverbound.AuthorizePacket
import blora.messaging.packet.serverbound.PlayerAuthorizationRequestPacket
import blora.messaging.packet.serverbound.PongPacket
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlinx.coroutines.*

@ChannelHandler.Sharable
class BloraServer(
    val port: Int
) : ChannelInboundHandlerAdapter() {

    private val channels: MutableMap<Channel, BloraConnection> = mutableMapOf()

    private val bossGroup: EventLoopGroup = NioEventLoopGroup()
    private val workerGroup: EventLoopGroup = NioEventLoopGroup()
    private lateinit var channelFuture: ChannelFuture

    private val waitingAuthorization: MutableMap<Channel, Job> = mutableMapOf()

    private var pingPongJob: Job? = null
    private val pongs: MutableMap<Channel, Long> = mutableMapOf()

    private val authorized: MutableMap<String, BloraConnection> = mutableMapOf()

    init {
        this.registerHandlers()
    }

    fun start() {
        val bootstrap = ServerBootstrap()
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.SO_BACKLOG, 128)
            .option(ChannelOption.SO_REUSEADDR, true)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_SNDBUF, 1024)
            .childOption(ChannelOption.SO_RCVBUF, 1024)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                @Throws(Exception::class)
                override fun initChannel(channel: SocketChannel) {
                    channels.put(channel, BloraConnection(channel))
                    channel.pipeline().addLast(this@BloraServer)
                }
            })

        this.channelFuture = bootstrap.bind(port).sync()

        pingPongJob = scope.launch {
            while (isActive) {
                delay(1000L)
                runCatching {
                    val waitingRemoval = mutableListOf<Channel>()
                    val current = System.currentTimeMillis()
                    for ((channel, lastPong) in pongs) {
                        if ((current - lastPong) > 30000L) {
                            BloraPlugin.log.info("由于连接超时，断开了一个 Blora 通讯服务器的连接")
                            channel.close()
                            waitingRemoval.add(channel)
                        }
                    }
                    waitingRemoval.forEach(pongs::remove)
                    broadcast(PingPacket())
                }.onFailure {
                    BloraPlugin.log.error("Ping/pong heartbeat failed", it)
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(Thread(this::close))
    }

    fun isConnected(serverName: String): Boolean {
        return authorized.containsKey(serverName)
    }

    fun isAuthorized(channel: Channel): Boolean {
        for ((_, connection) in this.authorized) {
            if (connection.channel == channel)
                return true
        }
        return false
    }

    fun broadcast(packet: Packet) {
        val byteBuf = Unpooled.buffer()
        byteBuf.writeInt(packet.type.id)
        packet.encode(byteBuf)
        val channels = this.channels.keys.toList()
        if (channels.isEmpty()) {
            byteBuf.release()
            return
        }
        // Each channel must receive its own buffer reference (shared buf would be freed after first write).
        channels.forEachIndexed { index, channel ->
            val payload = if (index == channels.lastIndex) byteBuf else byteBuf.retainedDuplicate()
            channel.writeAndFlush(payload)
        }
    }

    fun close() {
        this.broadcast(ShutdownPacket)
        this.waitingAuthorization.values.forEach(Job::cancel)
        this.pingPongJob?.cancel()
        this.channelFuture.channel().close()
        this.bossGroup.shutdownGracefully()
        this.workerGroup.shutdownGracefully()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        pongs[channel] = System.currentTimeMillis()
        this.waitingAuthorization[channel] = scope.launch {
            delay(30000L)
            for ((_, connection) in authorized) {
                if (connection.channel == channel) {
                    return@launch
                }
            }
            BloraPlugin.log.info("由于验证超时关闭了一个通信服务器的连接")
            channels[channel]!!.send(UnauthorizedPacket)
            channel.close()
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is ByteBuf) {
            val packetId = msg.readInt()
            val channel = ctx.channel()
            val connection = channels[channel]!!
            val packetType = PacketType.fromId(packetId)

            if (packetType == null)
                return

            val packet = packetType.packetConstructor()
            packet.decode(msg)

            PacketHandlerManager.handle(connection, packet)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, error: Throwable) {
        BloraPlugin.log.error("Blora 通讯发生错误：${error.message}")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        this.channels.remove(channel)
        this.pongs.remove(channel)
        this.waitingAuthorization.remove(channel)?.cancel()
        var name: String? = null
        for ((serverName, connection) in authorized) {
            if (connection.channel == channel) {
                BloraPlugin.log.info("服务器 $serverName 的 Blora 通讯系统断开")
                name = serverName
                break
            }
        }
        if (name != null) {
            this.authorized.remove(name)
        }
    }

    private fun registerHandlers() {
        PacketHandlerManager.register(false, PacketType.PONG, object : PacketHandler<PongPacket> {
            override fun handlePacket(
                connection: BloraConnection,
                packet: PongPacket
            ) {
                this@BloraServer.pongs[connection.channel] = System.currentTimeMillis()
            }
        })
        PacketHandlerManager.register(false, PacketType.AUTHORIZE, object : PacketHandler<AuthorizePacket> {
            override fun handlePacket(
                connection: BloraConnection,
                packet: AuthorizePacket
            ) {
                val serverOptional = BloraPlugin.proxyServer.getServer(packet.serverName)
                if (serverOptional.isEmpty) {
                    connection.send(AuthorizationFailedPacket)
                    println("server not exists")
                    return
                }
                /* FIXME: a better check
                val server = serverOptional.get()
                if (connection.address != server.serverInfo.address) {
                    println(connection.address)
                    println(server.serverInfo.address)
                    connection.send(AuthorizationFailedPacket)
                    println("host not match")
                    return
                }*/
                if (packet.password != BloraPlugin.configuration.messaging.password) {
                    println("wrong password")
                    connection.send(AuthorizationFailedPacket)
                    return
                }
                connection.send(AuthorizedPacket)
                this@BloraServer.authorized[packet.serverName] = connection
                this@BloraServer.waitingAuthorization.remove(connection.channel)?.cancel()
                BloraPlugin.log.info("服务器 ${packet.serverName} 的 Blora 通讯系统成功连接")
            }
        })
        PacketHandlerManager.register(true, PacketType.DEBUG_MESSAGE, object : PacketHandler<DebugMessagePacket> {
            override fun handlePacket(
                connection: BloraConnection,
                packet: DebugMessagePacket
            ) {
                BloraPlugin.log.info("收到了调试信息：${packet.message}")
            }
        })
        PacketHandlerManager.register(
            true,
            PacketType.PLAYER_AUTHORIZATION_REQUEST,
            object : PacketHandler<PlayerAuthorizationRequestPacket> {
                override fun handlePacket(
                    connection: BloraConnection,
                    packet: PlayerAuthorizationRequestPacket
                ) {
                    val player = BloraPlugin.proxyServer.getPlayer(packet.playerName)
                    player.ifPresent {
                        val response = PlayerAuthorizationResponsePacket()
                        response.playerName = packet.playerName
                        response.authorized = BloraAuthorization.isAuthorized(it)
                        connection.send(response)
                    }
                }
            })
        PacketHandlerManager.register(
            true,
            PacketType.RELOAD_CONFIGURATION,
            object : PacketHandler<ReloadConfigurationPacket> {
                override fun handlePacket(
                    connection: BloraConnection,
                    packet: ReloadConfigurationPacket
                ) {
                    this@BloraServer.broadcast(packet)
                    BloraPlugin.reloadConfiguration()
                }
            })
    }

    companion object {

        internal val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    }

}