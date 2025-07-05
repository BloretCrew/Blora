package blora.messaging

import blora.BloraPlugin
import blora.authorization.BloraAuthorization
import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import blora.messaging.packet.clientbound.*
import blora.messaging.packet.common.DebugMessagePacket
import blora.messaging.packet.common.PingPacket
import blora.messaging.packet.serverbound.AuthorizePacket
import blora.messaging.packet.serverbound.PlayerAuthorizationRequestPacket
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlinx.coroutines.*

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
            delay(1000L)
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
        this.channels.keys.forEach { channel -> channel.writeAndFlush(byteBuf) }
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
            when (packetId) {
                PacketType.PONG.id -> {
                    this.pongs[channel] = System.currentTimeMillis()
                }

                PacketType.AUTHORIZE.id -> {
                    val connection = channels[channel]!!
                    val authorizePacket = AuthorizePacket().apply { this.decode(msg) }
                    val serverOptional = BloraPlugin.proxyServer.getServer(authorizePacket.serverName)
                    if (serverOptional.isEmpty) {
                        connection.send(AuthorizationFailedPacket)
                        println("server not exists")
                        return
                    }
                    val server = serverOptional.get()
                    /* FIXME: a better check
                    if (connection.address != server.serverInfo.address) {
                        println(connection.address)
                        println(server.serverInfo.address)
                        connection.send(AuthorizationFailedPacket)
                        println("host not match")
                        return
                    }*/
                    if (authorizePacket.password != BloraPlugin.configuration.messageing.password) {
                        println("wrong password")
                        connection.send(AuthorizationFailedPacket)
                        return
                    }
                    connection.send(AuthorizedPacket)
                    this.authorized[authorizePacket.serverName] = connection
                    this.waitingAuthorization.remove(channel)?.cancel()
                    BloraPlugin.log.info("服务器 ${authorizePacket.serverName} 的 Blora 通讯系统成功连接")
                }

                PacketType.DEBUG_MESSAGE.id -> {
                    if (!this.isAuthorized(channel))
                        return
                    val debugMessagePacket = DebugMessagePacket()
                    debugMessagePacket.decode(msg)
                    BloraPlugin.log.info("收到了调试信息：${debugMessagePacket.message}")
                }

                PacketType.PLAYER_AUTHORIZATION_REQUEST.id -> {
                    if (!this.isAuthorized(channel))
                        return
                    val packet = PlayerAuthorizationRequestPacket()
                    packet.decode(msg)
                    val player = BloraPlugin.proxyServer.getPlayer(packet.playerName)
                    player.ifPresent {
                        val response = PlayerAuthorizationResponsePacket()
                        response.playerName = packet.playerName
                        response.authorized = BloraAuthorization.isAuthorized(it)
                        connection.send(response)
                    }
                }
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, error: Throwable) {
        BloraPlugin.log.error("Blora 通讯发生错误：${error.message}")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        this.channels.remove(channel)
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

    companion object {

        internal val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    }

}