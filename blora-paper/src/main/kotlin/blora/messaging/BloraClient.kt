package blora.messaging

import blora.listener.UnauthorizedListener
import blora.messaging.packet.Packet
import blora.messaging.packet.PacketHandler
import blora.messaging.packet.PacketHandlerManager
import blora.messaging.packet.PacketType
import blora.messaging.packet.clientbound.*
import blora.messaging.packet.common.DebugMessagePacket
import blora.messaging.packet.common.ReloadConfigurationPacket
import blora.messaging.packet.serverbound.AuthorizePacket
import blora.messaging.packet.serverbound.PongPacket
import blora.plugin.BloraPlugin
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.InetAddress
import java.net.InetSocketAddress

@ChannelHandler.Sharable
class BloraClient(
    val address: InetAddress,
    val port: Int
) : ChannelInboundHandlerAdapter() {


    private val group: EventLoopGroup = NioEventLoopGroup()
    private val bootstrap = Bootstrap()
    private var connection: BloraConnection

    private var connected = true

    init {
        this.registerHandlers()

        bootstrap.group(this.group)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_SNDBUF, 1024)
            .option(ChannelOption.SO_RCVBUF, 1024)
            .handler(object : ChannelInitializer<SocketChannel>() {
                @Throws(Exception::class)
                override fun initChannel(channel: SocketChannel) {
                    channel.pipeline().addLast(this@BloraClient)
                }
            })
        this.connection = BloraConnection(
            bootstrap.connect(InetSocketAddress(address, port)).sync().channel()
        )
    }

    fun reconnect() {
        if (this.connected)
            return
        this.connection = BloraConnection(
            bootstrap.connect(InetSocketAddress(address, port)).sync().channel()
        )
    }

    fun isConnected(): Boolean {
        return this.connection.channel.isWritable
    }

    fun send(packet: Packet) {
        this.connection.send(packet)
    }

    fun close() {
        this.connection.channel.close()
        this.group.shutdownGracefully()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        BloraConnection(ctx.channel()).send(AuthorizePacket().apply {
            this.serverName = BloraPlugin.configuration.messaging.serverName
            this.password = BloraPlugin.configuration.messaging.password
        })
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is ByteBuf) {
            val packetId = msg.readInt()
            val packetType = PacketType.fromId(packetId)

            if (packetType == null)
                return

            val packet = packetType.packetConstructor()
            packet.decode(msg)

            PacketHandlerManager.handle(connection, packet)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        BloraPlugin.slF4JLogger.error("Blora 通讯发生错误：${cause.message}")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        connected = false
        BloraPlugin.slF4JLogger.error("Blora 服务器通讯断连")
        ctx.channel().close()
    }

    private fun registerHandlers() {
        PacketHandlerManager.register(PacketType.PING, object : PacketHandler<PingPacket> {
            override fun handlePacket(
                connection: BloraConnection,
                packet: PingPacket
            ) {
                connection.send(PongPacket(packet.time))
            }
        })
        PacketHandlerManager.register(PacketType.UNAUTHORIZED, object : PacketHandler<UnauthorizedPacket> {
            override fun handlePacket(
                connection: BloraConnection,
                packet: UnauthorizedPacket
            ) {
                BloraPlugin.slF4JLogger.error("Blora 服务器通讯未及时验证")
            }
        })
        PacketHandlerManager.register(
            PacketType.AUTHORIZATION_FAILED,
            object : PacketHandler<AuthorizationFailedPacket> {
                override fun handlePacket(
                    connection: BloraConnection,
                    packet: AuthorizationFailedPacket
                ) {
                    BloraPlugin.slF4JLogger.error("Blora 服务器通讯身份验证不通过，无法连接")
                }
            })
        PacketHandlerManager.register(PacketType.AUTHORIZED, object : PacketHandler<AuthorizedPacket> {
            override fun handlePacket(
                connection: BloraConnection,
                packet: AuthorizedPacket
            ) {
                BloraPlugin.slF4JLogger.info("Blora 服务器通讯成功连接")
            }
        })
        PacketHandlerManager.register(PacketType.SHUTDOWN, object : PacketHandler<ShutdownPacket> {
            override fun handlePacket(
                connection: BloraConnection,
                packet: ShutdownPacket
            ) {
                BloraPlugin.slF4JLogger.info("Blora 代理服务器已关闭")
            }
        })
        PacketHandlerManager.register(PacketType.DEBUG_MESSAGE, object : PacketHandler<DebugMessagePacket> {
            override fun handlePacket(
                connection: BloraConnection,
                packet: DebugMessagePacket
            ) {
                BloraPlugin.slF4JLogger.info("收到了调试信息：${packet.message}")
            }
        })
        PacketHandlerManager.register(
            PacketType.PLAYER_AUTHORIZATION_RESPONSE,
            object : PacketHandler<PlayerAuthorizationResponsePacket> {
                override fun handlePacket(
                    connection: BloraConnection,
                    packet: PlayerAuthorizationResponsePacket
                ) {
                    UnauthorizedListener.updatePlayerStatus(packet)
                }
            })
        PacketHandlerManager.register(
            PacketType.PLAYER_AUTHORIZATION_UPDATE,
            object : PacketHandler<PlayerAuthorizationUpdatePacket> {
                override fun handlePacket(
                    connection: BloraConnection,
                    packet: PlayerAuthorizationUpdatePacket
                ) {
                    UnauthorizedListener.updatePlayerStatus(packet)
                }
            })
        PacketHandlerManager.register(
            PacketType.RELOAD_CONFIGURATION,
            object : PacketHandler<ReloadConfigurationPacket> {
                override fun handlePacket(
                    connection: BloraConnection,
                    packet: ReloadConfigurationPacket
                ) {
                    BloraPlugin.reloadConfiguration()
                }
            })
    }

}