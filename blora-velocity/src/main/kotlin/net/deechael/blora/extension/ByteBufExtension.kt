package net.deechael.blora.extension

import io.netty.buffer.ByteBuf

const val SEGMENT_BITS = 0x7F
const val CONTINUE_BIT = 0x80

fun ByteBuf.writeVarInt(value: Int): Int {
    var input = value
    var length = 1
    while (true) {
        if ((input and SEGMENT_BITS.inv()) == 0) {
            this.writeByte(input)
            length++
            return length
        }

        this.writeByte((input and SEGMENT_BITS) or CONTINUE_BIT)
        input = input ushr 7
        length++
    }
}

fun ByteBuf.readVarInt(): Int {
    var value = 0
    var position = 0
    var currentByte: Byte

    while (true) {
        currentByte = readByte()
        value = value or ((currentByte.toInt() and SEGMENT_BITS) shl position)

        if ((currentByte.toInt() and CONTINUE_BIT) == 0) break

        position += 7

        if (position >= 32)
            throw RuntimeException("VarInt is too big")
    }

    return value
}