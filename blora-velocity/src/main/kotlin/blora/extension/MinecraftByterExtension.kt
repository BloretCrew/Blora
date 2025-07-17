package blora.extension

import blora.bytes.Byter
import blora.util.positiveCeilDiv
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.netty.handler.codec.EncoderException
import java.util.*


fun Byter.writeUtf(string: String, maxLength: Int) {
    Utf8String.write(this, string, maxLength)
}

fun <E : Enum<E>> Byter.writeEnumSet(enumSet: EnumSet<E>, enumClass: Class<E>) {
    val enums = (enumClass.getEnumConstants()) as Array<E?>
    val bitSet = BitSet(enums.size)

    for (i in enums.indices) {
        bitSet.set(i, enumSet.contains(enums[i]))
    }

    this.writeFixedBitSet(bitSet, enums.size)
}


fun Byter.writeFixedBitSet(bitSet: BitSet, size: Int) {
    if (bitSet.length() > size) {
        val length = bitSet.length()
        throw EncoderException("BitSet is larger than expected size ($length>$size)")
    } else {
        val bytes = bitSet.toByteArray()
        this.writeBytes(bytes.copyOf(positiveCeilDiv(size, 8)))
    }
}

object Utf8String {
    fun write(buffer: Byter, string: CharSequence, maxLength: Int) {
        if (string.length > maxLength) {
            val var10002 = string.length
            throw EncoderException("String too big (was $var10002 characters, max $maxLength)")
        } else {
            val i = ByteBufUtil.utf8MaxBytes(string)
            val byteBuf = Unpooled.buffer(i)

            try {
                val i1 = ByteBufUtil.writeUtf8(byteBuf, string)
                val i2 = ByteBufUtil.utf8MaxBytes(maxLength)
                if (i1 > i2) {
                    throw EncoderException("String too big (was $i1 bytes encoded, max $i2)")
                }

                buffer.writeVarInt(i1)
                buffer.writeBytes(byteBuf)
            } finally {
                byteBuf.release()
            }
        }
    }
}
