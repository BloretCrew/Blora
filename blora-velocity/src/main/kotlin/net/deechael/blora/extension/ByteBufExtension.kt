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