package blora.protocol.packet

import blora.extension.readVarInt
import blora.serialization.nbt.compound
import blora.serialization.nbt.readRestToNbt
import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.proxy.connection.MinecraftSessionHandler
import com.velocitypowered.proxy.protocol.MinecraftPacket
import com.velocitypowered.proxy.protocol.ProtocolUtils
import io.netty.buffer.ByteBuf
import net.benwoodworth.knbt.NbtTag

class CustomClickAction() : MinecraftPacket {

    var id: String = ""
    var payload: NbtTag = compound { }

    var copiedByteBuf: ByteBuf? = null

    constructor(id: String, payload: NbtTag) : this() {
        this.id = id
        this.payload = payload
    }

    override fun decode(
        buf: ByteBuf,
        direction: ProtocolUtils.Direction?,
        protocolVersion: ProtocolVersion?
    ) {
        this.copiedByteBuf = buf.copy()

        val length = buf.readVarInt()
        this.id = (0 until length).map { buf.readByte() }.toByteArray().decodeToString()
        buf.readVarInt() // read nbt size to release it
        this.payload = buf.readRestToNbt()
    }

    override fun encode(
        buf: ByteBuf,
        direction: ProtocolUtils.Direction,
        protocolVersion: ProtocolVersion
    ) {
        buf.writeBytes(this.copiedByteBuf)
        // FIXME: cannot encode with normal steps, to be fixed
        /*
        val bytes = this.id.encodeToByteArray()
        buf.writeVarInt(bytes.size)
        buf.writeBytes(bytes)
        val byteArray = this.payload.toByteArray()
        buf.writeVarInt(byteArray.size)
        buf.writeBytes(byteArray)*/
    }

    override fun handle(handler: MinecraftSessionHandler): Boolean {
        handler.handleGeneric(this)
        return true
    }

}