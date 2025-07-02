package net.deechael.blora.serialization.nbt

import io.netty.buffer.ByteBuf
import net.benwoodworth.knbt.NbtByte
import net.benwoodworth.knbt.NbtByteArray
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtDouble
import net.benwoodworth.knbt.NbtFloat
import net.benwoodworth.knbt.NbtInt
import net.benwoodworth.knbt.NbtIntArray
import net.benwoodworth.knbt.NbtList
import net.benwoodworth.knbt.NbtLong
import net.benwoodworth.knbt.NbtLongArray
import net.benwoodworth.knbt.NbtShort
import net.benwoodworth.knbt.NbtString
import net.benwoodworth.knbt.NbtTag

fun NbtTag.getId(): Byte {
    return when (this) {
        is NbtByte -> 1
        is NbtShort -> 2
        is NbtInt -> 3
        is NbtLong -> 4
        is NbtFloat -> 5
        is NbtDouble -> 6
        is NbtByteArray -> 7
        is NbtString -> 8
        is NbtList<*> -> 9
        is NbtCompound -> 10
        is NbtIntArray -> 11
        is NbtLongArray -> 12
    }
}

fun NbtTag.writeToByteBuf(name: String, byteBuf: ByteBuf) {
    byteBuf.writeByte(this.getId().toInt())
    NbtString(name).writeValue(byteBuf)
    this.writeValue(byteBuf)
}

fun NbtTag.writeValue(byteBuf: ByteBuf) {
    if (this is NbtCompound) {
        for ((name, tag) in this) {
            tag.writeToByteBuf(name, byteBuf)
        }
        byteBuf.writeByte(0)
    } else if (this is NbtList<*>) {
        if (this.isEmpty()) {
            byteBuf.writeByte(0)
            byteBuf.writeInt(0)
        } else {
            byteBuf.writeByte(this[0].getId().toInt())
            for (tag in this) {
                tag.writeValue(byteBuf)
            }
        }
    } else if (this is NbtString) {
        val bytes = this.value.encodeToByteArray()
        byteBuf.writeShort(bytes.size)
        byteBuf.writeBytes(bytes)
    } else if (this is NbtByte) {
        byteBuf.writeByte(this.value.toInt())
    } else if (this is NbtShort) {
        byteBuf.writeShort(this.value.toInt())
    } else if (this is NbtInt) {
        byteBuf.writeInt(this.value)
    } else if (this is NbtLong) {
        byteBuf.writeLong(this.value)
    } else if (this is NbtFloat) {
        byteBuf.writeFloat(this.value)
    } else if (this is NbtDouble) {
        byteBuf.writeDouble(this.value)
    } else if (this is NbtByteArray) {
        byteBuf.writeInt(this.size)
        for (byte in this) {
            byteBuf.writeByte(byte.toInt())
        }
    } else if (this is NbtIntArray) {
        byteBuf.writeInt(this.size)
        for (int in this) {
            byteBuf.writeInt(int)
        }
    } else if (this is NbtLongArray) {
        byteBuf.writeInt(this.size)
        for (long in this) {
            byteBuf.writeLong(long)
        }
    }
}

fun compound(builder: NbtCompoundBuilder.() -> Unit): NbtCompound {
    return NbtCompound(NbtCompoundBuilder().apply(builder).contents.toMap())
}

class NbtCompoundBuilder {

    internal val contents: MutableMap<String, NbtTag> = mutableMapOf()

    infix fun String.eq(value: String) {
        contents[this] = NbtString(value)
    }

    infix fun String.eq(value: Boolean) {
        contents[this] = NbtByte(value)
    }

    infix fun String.eq(value: Byte) {
        contents[this] = NbtByte(value)
    }

    infix fun String.eq(value: Short) {
        contents[this] = NbtShort(value)
    }

    infix fun String.eq(value: Int) {
        contents[this] = NbtInt(value)
    }

    infix fun String.eq(value: Long) {
        contents[this] = NbtLong(value)
    }

    infix fun String.eq(value: Float) {
        contents[this] = NbtFloat(value)
    }

    infix fun String.eq(value: Double) {
        contents[this] = NbtDouble(value)
    }

    infix fun String.eq(tag: NbtTag) {
        contents[this] = tag
    }

}