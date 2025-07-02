package net.deechael.blora.dialog

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import net.benwoodworth.knbt.NbtCompound
import net.deechael.blora.extension.writeVarInt
import net.deechael.blora.serialization.nbt.writeToByteBuf
import net.deechael.blora.serialization.serializer.jsonComponentSupport
import net.deechael.blora.serialization.serializer.keySupport

@Serializable
sealed class Dialog {

    abstract fun toNBT(): NbtCompound

}

@OptIn(ExperimentalSerializationApi::class)
val DIALOG_JSON = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    classDiscriminatorMode = ClassDiscriminatorMode.NONE
    namingStrategy = JsonNamingStrategy.SnakeCase
    decodeEnumsCaseInsensitive = true // useless for now
    explicitNulls = false
    serializersModule = SerializersModule {
        jsonComponentSupport()
        keySupport()
    }
}

fun Dialog.asPacket(): ByteBuf {
    val byteBuf = Unpooled.buffer()
    byteBuf.writeVarInt(0x85)
    byteBuf.writeVarInt(0)
    this.toNBT().writeToByteBuf("", byteBuf)
/*
    val bytes = Nbt {
        variant = NbtVariant.Java
        compression = NbtCompression.None
    }.encodeToByteArray(compound {
        "" eq this@asPacket.toNBT()
    })

    byteBuf = Unpooled.buffer(packetIdLength + idLength + bytes.size)

    byteBuf.writeVarInt(0x85)
    byteBuf.writeVarInt(0)
    byteBuf.writeBytes(
        bytes
    )
    val file = File(BloraPlugin.dataDirectory.toFile(), "debug.nbt")
    if (!file.exists()) {
        file.createNewFile()
    }
    file.writeBytes(bytes)
    */
    return byteBuf
}

fun Dialog.encodeToString(): String {
    return DIALOG_JSON.encodeToString(this)
}