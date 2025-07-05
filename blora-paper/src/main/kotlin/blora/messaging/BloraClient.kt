package blora.messaging

import blora.messaging.packet.Packet
import blora.messaging.packet.PacketType
import blora.messaging.packet.common.DebugMessagePacket
import blora.messaging.packet.common.PingPacket
import blora.messaging.packet.common.PongPacket
import blora.messaging.packet.serverbound.AuthorizePacket
import blora.plugin.BloraPlugin
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.InetAddress
import java.net.InetSocketAddress


class BloraClient(
    val address: InetAddress,
    val port: Int
) : ChannelInboundHandlerAdapter() {


    private val group: EventLoopGroup = NioEventLoopGroup()
    private val connection: BloraConnection

    init {
        val bootstrap = Bootstrap()
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

    fun send(packet: Packet) {
        this.connection.send(packet)
    }

    fun close() {
        this.connection.channel.close()
        this.group.shutdownGracefully()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        BloraConnection(ctx.channel()).send(AuthorizePacket().apply {
            this.serverName = BloraPlugin.configuration.messageing.serverName
            this.password = BloraPlugin.configuration.messageing.password
        })
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is ByteBuf) {
            val packetId = msg.readInt()
            when (packetId) {
                PacketType.PING.id -> {
                    val pingPacket = PingPacket()
                    pingPacket.decode(msg)
                    val pongPacket = PongPacket()
                    pongPacket.pingTime = pingPacket.time
                    this.connection.send(pongPacket)
                }

                PacketType.UNAUTHORIZED.id -> {
                    BloraPlugin.slF4JLogger.error("Blora 服务器通讯未及时验证")
                }

                PacketType.AUTHORIZATION_FAILED.id -> {
                    BloraPlugin.slF4JLogger.error("Blora 服务器通讯身份验证不通过，无法连接")
                }

                PacketType.AUTHORIZED.id -> {
                    BloraPlugin.slF4JLogger.info("Blora 服务器通讯成功连接")
                }

                PacketType.SHUTDOWN.id -> {
                    BloraPlugin.slF4JLogger.info("Blora 服务器通讯已关闭")
                }

                PacketType.DEBUG_MESSAGE.id -> {
                    val debugMessagePacket = DebugMessagePacket()
                    debugMessagePacket.decode(msg)
                    BloraPlugin.slF4JLogger.info("收到了调试信息：${debugMessagePacket.message}")
                }
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        BloraPlugin.slF4JLogger.error("Blora 通讯发生错误：${cause.message}")
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        BloraPlugin.slF4JLogger.error("Blora 服务器通讯断连")
    }

}