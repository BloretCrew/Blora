package blora.protocol.packet

import blora.extension.readVarInt
import blora.serialization.nbt.readRestToNbt
import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.proxy.connection.MinecraftSessionHandler
import com.velocitypowered.proxy.protocol.MinecraftPacket
import com.velocitypowered.proxy.protocol.ProtocolUtils
import io.netty.buffer.ByteBuf
import net.benwoodworth.knbt.NbtTag

class CustomClickAction : MinecraftPacket {

    var id: String? = null
        private set
    var payload: NbtTag? = null
        private set

    override fun decode(
        buf: ByteBuf,
        direction: ProtocolUtils.Direction?,
        protocolVersion: ProtocolVersion?
    ) {
        val length = buf.readVarInt()
        this.id = (0 until length).map { buf.readByte() }.toByteArray().decodeToString()
        buf.readVarInt() // read nbt size to release it
        this.payload = buf.readRestToNbt()
        /*
        println("unsigned value is ${buf.readUnsignedByte()}")
        File("another_test.nbt")
            .writeBytes(
                (0 until buf.readableBytes()).map { buf.readByte() }.toByteArray()
            )*/
    }

    override fun encode(
        buf: ByteBuf?,
        direction: ProtocolUtils.Direction?,
        protocolVersion: ProtocolVersion?
    ) {
    }

    override fun handle(handler: MinecraftSessionHandler): Boolean {
        return true
    }

}