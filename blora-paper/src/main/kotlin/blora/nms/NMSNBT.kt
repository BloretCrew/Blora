package blora.nms

import blora.serialization.nbt.compound
import net.benwoodworth.knbt.*
import net.minecraft.nbt.*

fun Tag?.toKnbt(): NbtTag? {
    if (this == null)
        return null
    return when (this) {
        is ByteTag -> NbtByte(this.value)
        is ShortTag -> NbtShort(this.value)
        is IntTag -> NbtInt(this.value)
        is LongTag -> NbtLong(this.value)
        is FloatTag -> NbtFloat(this.value)
        is DoubleTag -> NbtDouble(this.value)
        is StringTag -> NbtString(this.value)
        is ByteArrayTag -> NbtByteArray(this.asByteArray)
        is IntArrayTag -> NbtIntArray(this.asIntArray)
        is LongArrayTag -> NbtLongArray(this.asLongArray)
        is CompoundTag -> compound {
            for ((key, tag) in this@toKnbt.entrySet()) {
                val realTag = tag.toKnbt()
                if (realTag != null) {
                    key eq realTag
                }
            }
        }

        is ListTag -> {
            if (this.isEmpty) {
                null
            } else {
                when (this.type) {
                    ByteTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtByte }.toList())
                    ShortTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtShort }.toList())
                    IntTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtInt }.toList())
                    LongTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtLong }.toList())
                    FloatTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtFloat }.toList())
                    DoubleTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtDouble }.toList())
                    StringTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtString }.toList())
                    ByteArrayTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtByteArray }.toList())
                    IntArrayTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtIntArray }.toList())
                    LongArrayTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtLongArray }.toList())
                    CompoundTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtCompound }.toList())
                    ListTag.TYPE -> NbtList(this.mapNotNull { it.toKnbt() }.map { it as NbtList<*> }.toList())
                    else -> null
                }
            }
        }

        is EndTag -> null
    }
}

fun NbtTag.toNMS(): Tag {
    return when (this) {
        is NbtByte -> {
            ByteTag.valueOf(this.value)
        }

        is NbtShort -> {
            ShortTag.valueOf(this.value)
        }

        is NbtInt -> {
            IntTag.valueOf(this.value)
        }

        is NbtLong -> {
            LongTag.valueOf(this.value)
        }

        is NbtFloat -> {
            FloatTag.valueOf(this.value)
        }

        is NbtDouble -> {
            DoubleTag.valueOf(this.value)
        }

        is NbtString -> {
            StringTag.valueOf(this.value)
        }

        is NbtByteArray -> {
            ByteArrayTag(this.toByteArray())
        }

        is NbtIntArray -> {
            IntArrayTag(this.toIntArray())
        }

        is NbtLongArray -> {
            LongArrayTag(this.toLongArray())
        }

        is NbtCompound -> {
            CompoundTag().apply {
                for ((key, tag) in this@toNMS) {
                    this.put(key, tag.toNMS())
                }
            }
        }

        is NbtList<*> -> {
            val list = ListTag()
            for (tag in this) {
                list.add(tag.toNMS())
            }
            list
        }
    }
}