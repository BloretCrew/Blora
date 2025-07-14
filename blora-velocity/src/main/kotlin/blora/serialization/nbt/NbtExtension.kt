package blora.serialization.nbt

import blora.bytes.Byter
import io.netty.buffer.ByteBuf
import net.benwoodworth.knbt.*

fun ByteBuf.readRestToNbt(): NbtTag {
    val id = this.readByte().toInt()
    val tag: NbtTag = when (id) {
        1 -> readNbtByte()
        2 -> readNbtShort()
        3 -> readNbtInt()
        4 -> readNbtLong()
        5 -> readNbtFloat()
        6 -> readNbtDouble()
        7 -> readNbtByteArray()
        8 -> readNbtString()
        9 -> readNbtList() ?: NbtCompound(mapOf())
        10 -> readNbtCompound()
        11 -> readNbtIntArray()
        12 -> readNbtLongArray()
        else -> NbtCompound(mapOf())
    }
    return tag
}

fun ByteBuf.readNbtCompound(): NbtCompound {
    return compound {
        var id = readByte().toInt()
        while (id != 0) {
            val name = readNbtString()
            val tag: NbtTag? = when (id) {
                1 -> readNbtByte()
                2 -> readNbtShort()
                3 -> readNbtInt()
                4 -> readNbtLong()
                5 -> readNbtFloat()
                6 -> readNbtDouble()
                7 -> readNbtByteArray()
                8 -> readNbtString()
                9 -> readNbtList()
                10 -> readNbtCompound()
                11 -> readNbtIntArray()
                12 -> readNbtLongArray()
                else -> null
            }
            if (tag != null) {
                name.value eq tag
            }
            id = readByte().toInt()
        }
    }
}

fun ByteBuf.readNbtList(): NbtList<*>? {
    val type = readByte().toInt()
    val size = readInt()
    return when (type) {
        1 -> {
            NbtList(
                (0 until size)
                    .map { readNbtByte() }
                    .toList())
        }

        2 -> {
            NbtList(
                (0 until size)
                    .map { readNbtShort() }
                    .toList())
        }

        3 -> {
            NbtList(
                (0 until size)
                    .map { readNbtInt() }
                    .toList())
        }

        4 -> {
            NbtList(
                (0 until size)
                    .map { readNbtLong() }
                    .toList())
        }

        5 -> {
            NbtList(
                (0 until size)
                    .map { readNbtFloat() }
                    .toList())
        }

        6 -> {
            NbtList(
                (0 until size)
                    .map { readNbtDouble() }
                    .toList())
        }

        7 -> {
            NbtList(
                (0 until size)
                    .map { readNbtByteArray() }
                    .toList())
        }

        8 -> {
            NbtList(
                (0 until size)
                    .map { readNbtString() }
                    .toList())
        }

        10 -> {
            NbtList(
                (0 until size)
                    .map { readNbtCompound() }
                    .toList())
        }

        11 -> {
            NbtList(
                (0 until size)
                    .map { readNbtIntArray() }
                    .toList())
        }

        12 -> {
            NbtList(
                (0 until size)
                    .map { readNbtLongArray() }
                    .toList())
        }

        else -> {
            null
        }
    }
}

fun ByteBuf.readNbtByte(): NbtByte {
    return NbtByte(this.readByte())
}

fun ByteBuf.readNbtShort(): NbtShort {
    return NbtShort(this.readShort())
}

fun ByteBuf.readNbtInt(): NbtInt {
    return NbtInt(this.readInt())
}

fun ByteBuf.readNbtLong(): NbtLong {
    return NbtLong(this.readLong())
}

fun ByteBuf.readNbtFloat(): NbtFloat {
    return NbtFloat(this.readFloat())
}

fun ByteBuf.readNbtDouble(): NbtDouble {
    return NbtDouble(this.readDouble())
}

fun ByteBuf.readNbtString(): NbtString {
    val length = this.readShort()
    return NbtString(
        this.readBytes(length.toInt()).toString(Charsets.UTF_8)
    )
}

fun ByteBuf.readNbtByteArray(): NbtByteArray {
    val size = this.readInt()
    return NbtByteArray(
        (0 until size).map {
            this.readByte()
        }.toByteArray()
    )
}

fun ByteBuf.readNbtIntArray(): NbtIntArray {
    val size = this.readInt()
    return NbtIntArray(
        (0 until size).map {
            this.readInt()
        }.toIntArray()
    )
}

fun ByteBuf.readNbtLongArray(): NbtLongArray {
    val size = this.readInt()
    return NbtLongArray(
        (0 until size).map {
            this.readLong()
        }.toLongArray()
    )
}

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

fun NbtTag.writeToByteBuf(name: String, byteBuf: Byter) {
    if (this is NbtList<*> && this.isEmpty())
        return
    byteBuf.writeByte(this.getId())
    NbtString(name).writeValue(byteBuf)
    this.writeValue(byteBuf)
}

fun NbtTag.writeToByteBuf(name: String, byteBuf: ByteBuf) {
    if (this is NbtList<*> && this.isEmpty())
        return
    byteBuf.writeByte(this.getId().toInt())
    NbtString(name).writeValue(byteBuf)
    this.writeValue(byteBuf)
}

fun NbtCompound.writeToByteBufRoot(byteBuf: ByteBuf) {
    byteBuf.writeByte(0x0a)
    this.writeValue(byteBuf)
}

fun NbtTag.toByteArray(): ByteArray {
    val byteBuf = Byter()
    when (this) {
        is NbtCompound -> {
            for ((name, tag) in this) {
                tag.writeToByteBuf(name, byteBuf)
            }
            byteBuf.writeByte(0)
        }

        is NbtList<*> -> {
            byteBuf.writeByte(this[0].getId())
            byteBuf.writeInt(this.size)
            for (index in 0 until this.size) {
                this[index].writeValue(byteBuf)
            }
        }

        is NbtString -> {
            val bytes = this.value.encodeToByteArray()
            byteBuf.writeShort(bytes.size.toShort())
            byteBuf.writeBytes(bytes)
        }

        is NbtByte -> {
            byteBuf.writeByte(this.value)
        }

        is NbtShort -> {
            byteBuf.writeShort(this.value)
        }

        is NbtInt -> {
            byteBuf.writeInt(this.value)
        }

        is NbtLong -> {
            byteBuf.writeLong(this.value)
        }

        is NbtFloat -> {
            byteBuf.writeFloat(this.value)
        }

        is NbtDouble -> {
            byteBuf.writeDouble(this.value)
        }

        is NbtByteArray -> {
            byteBuf.writeInt(this.size)
            for (byte in this) {
                byteBuf.writeByte(byte)
            }
        }

        is NbtIntArray -> {
            byteBuf.writeInt(this.size)
            for (int in this) {
                byteBuf.writeInt(int)
            }
        }

        is NbtLongArray -> {
            byteBuf.writeInt(this.size)
            for (long in this) {
                byteBuf.writeLong(long)
            }
        }
    }
    return byteBuf.byteArray
}

fun NbtTag.writeValue(byteBuf: ByteBuf) {
    when (this) {
        is NbtCompound -> {
            for ((name, tag) in this) {
                tag.writeToByteBuf(name, byteBuf)
            }
            byteBuf.writeByte(0)
        }

        is NbtList<*> -> {
            byteBuf.writeByte(this[0].getId().toInt())
            byteBuf.writeInt(this.size)
            for (index in 0 until this.size) {
                this[index].writeValue(byteBuf)
            }
        }

        is NbtString -> {
            val bytes = this.value.encodeToByteArray()
            byteBuf.writeShort(bytes.size)
            byteBuf.writeBytes(bytes)
        }

        is NbtByte -> {
            byteBuf.writeByte(this.value.toInt())
        }

        is NbtShort -> {
            byteBuf.writeShort(this.value.toInt())
        }

        is NbtInt -> {
            byteBuf.writeInt(this.value)
        }

        is NbtLong -> {
            byteBuf.writeLong(this.value)
        }

        is NbtFloat -> {
            byteBuf.writeFloat(this.value)
        }

        is NbtDouble -> {
            byteBuf.writeDouble(this.value)
        }

        is NbtByteArray -> {
            byteBuf.writeInt(this.size)
            for (byte in this) {
                byteBuf.writeByte(byte.toInt())
            }
        }

        is NbtIntArray -> {
            byteBuf.writeInt(this.size)
            for (int in this) {
                byteBuf.writeInt(int)
            }
        }

        is NbtLongArray -> {
            byteBuf.writeInt(this.size)
            for (long in this) {
                byteBuf.writeLong(long)
            }
        }
    }
}

fun NbtTag.writeValue(byteBuf: Byter) {
    when (this) {
        is NbtCompound -> {
            for ((name, tag) in this) {
                tag.writeToByteBuf(name, byteBuf)
            }
            byteBuf.writeByte(0)
        }

        is NbtList<*> -> {
            byteBuf.writeByte(this[0].getId())
            byteBuf.writeInt(this.size)
            for (index in 0 until this.size) {
                this[index].writeValue(byteBuf)
            }
        }

        is NbtString -> {
            val bytes = this.value.encodeToByteArray()
            byteBuf.writeShort(bytes.size.toShort())
            byteBuf.writeBytes(bytes)
        }

        is NbtByte -> {
            byteBuf.writeByte(this.value)
        }

        is NbtShort -> {
            byteBuf.writeShort(this.value)
        }

        is NbtInt -> {
            byteBuf.writeInt(this.value)
        }

        is NbtLong -> {
            byteBuf.writeLong(this.value)
        }

        is NbtFloat -> {
            byteBuf.writeFloat(this.value)
        }

        is NbtDouble -> {
            byteBuf.writeDouble(this.value)
        }

        is NbtByteArray -> {
            byteBuf.writeInt(this.size)
            for (byte in this) {
                byteBuf.writeByte(byte)
            }
        }

        is NbtIntArray -> {
            byteBuf.writeInt(this.size)
            for (int in this) {
                byteBuf.writeInt(int)
            }
        }

        is NbtLongArray -> {
            byteBuf.writeInt(this.size)
            for (long in this) {
                byteBuf.writeLong(long)
            }
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