package blora.extension

import java.nio.ByteBuffer
import java.nio.ByteOrder


fun ByteArray.toShortBE(): Short = ByteBuffer.wrap(this).apply {
    order(ByteOrder.BIG_ENDIAN)
}.short

fun ByteArray.toShortLE(): Short = ByteBuffer.wrap(this).apply {
    order(ByteOrder.LITTLE_ENDIAN)
}.short

fun ByteArray.toIntBE(): Int = ByteBuffer.wrap(this).apply {
    order(ByteOrder.BIG_ENDIAN)
}.int

fun ByteArray.toIntLE(): Int = ByteBuffer.wrap(this).apply {
    order(ByteOrder.LITTLE_ENDIAN)
}.int

fun ByteArray.toLongBE(): Long = ByteBuffer.wrap(this).apply {
    order(ByteOrder.BIG_ENDIAN)
}.long

fun ByteArray.toLongLE(): Long = ByteBuffer.wrap(this).apply {
    order(ByteOrder.LITTLE_ENDIAN)
}.long

fun ByteArray.toFloatBE(): Float = ByteBuffer.wrap(this).apply {
    order(ByteOrder.BIG_ENDIAN)
}.float

fun ByteArray.toFloatLE(): Float = ByteBuffer.wrap(this).apply {
    order(ByteOrder.LITTLE_ENDIAN)
}.float

fun ByteArray.toDoubleBE(): Double = ByteBuffer.wrap(this).apply {
    order(ByteOrder.BIG_ENDIAN)
}.double

fun ByteArray.toDoubleLE(): Double = ByteBuffer.wrap(this).apply {
    order(ByteOrder.LITTLE_ENDIAN)
}.double

fun ByteArray.toUShortBE(): UShort {
    require(size >= 2) { "需要至少2字节" }
    return ((this[0].toInt() and 0xFF shl 8) or (this[1].toInt() and 0xFF)).toUShort()
}

fun ByteArray.toUShortLE(): UShort {
    require(size >= 2) { "需要至少2字节" }
    return ((this[1].toInt() and 0xFF shl 8) or (this[0].toInt() and 0xFF)).toUShort()
}

fun ByteArray.toUIntBE(): UInt {
    require(size >= 4) { "需要至少4字节" }
    return ((this[0].toUInt() and 0xFFu shl 24) or
            (this[1].toUInt() and 0xFFu shl 16) or
            (this[2].toUInt() and 0xFFu shl 8) or
            (this[3].toUInt() and 0xFFu))
}

fun ByteArray.toUIntLE(): UInt {
    require(size >= 4) { "需要至少4字节" }
    return ((this[3].toUInt() and 0xFFu shl 24) or
            (this[2].toUInt() and 0xFFu shl 16) or
            (this[1].toUInt() and 0xFFu shl 8) or
            (this[0].toUInt() and 0xFFu))
}

fun ByteArray.toULongBE(): ULong {
    require(size >= 8) { "需要至少8字节" }
    return ((this[0].toULong() and 0xFFu shl 56) or
            (this[1].toULong() and 0xFFu shl 48) or
            (this[2].toULong() and 0xFFu shl 40) or
            (this[3].toULong() and 0xFFu shl 32) or
            (this[4].toULong() and 0xFFu shl 24) or
            (this[5].toULong() and 0xFFu shl 16) or
            (this[6].toULong() and 0xFFu shl 8) or
            (this[7].toULong() and 0xFFu))
}

fun ByteArray.toULongLE(): ULong {
    require(size >= 8) { "需要至少8字节" }
    return ((this[7].toULong() and 0xFFu shl 56) or
            (this[6].toULong() and 0xFFu shl 48) or
            (this[5].toULong() and 0xFFu shl 40) or
            (this[4].toULong() and 0xFFu shl 32) or
            (this[3].toULong() and 0xFFu shl 24) or
            (this[2].toULong() and 0xFFu shl 16) or
            (this[1].toULong() and 0xFFu shl 8) or
            (this[0].toULong() and 0xFFu))
}