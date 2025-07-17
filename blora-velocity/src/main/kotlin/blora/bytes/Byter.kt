package blora.bytes

import blora.extension.*
import io.netty.buffer.ByteBuf
import java.nio.charset.Charset
import java.util.*

private const val SEGMENT_BITS = 0x7F
private const val CONTINUE_BIT = 0x80

class Byter() {

    constructor(bytes: ByteArray) : this() {
        this.bytes.addAll(bytes.toList())
    }

    private val bytes: MutableList<Byte> = arrayListOf()

    val byteArray: ByteArray
        get() = bytes.toByteArray()

    var readerIndex: Int = 0
        private set

    fun writeBoolean(boolean: Boolean) {
        this.bytes.add(if (boolean) 1 else 0)
    }

    fun writeByte(byte: Byte) {
        this.bytes.add(byte)
    }

    fun writeShort(short: Short) {
        this.bytes.addAll(short.toByteArrayBE().toList())
    }

    fun writeShortLE(short: Short) {
        this.bytes.addAll(short.toByteArrayLE().toList())
    }

    fun writeInt(int: Int) {
        this.bytes.addAll(int.toByteArrayBE().toList())
    }

    fun writeIntLE(int: Int) {
        this.bytes.addAll(int.toByteArrayLE().toList())
    }

    fun writeLong(long: Long) {
        this.bytes.addAll(long.toByteArrayBE().toList())
    }

    fun writeLongLE(long: Long) {
        this.bytes.addAll(long.toByteArrayLE().toList())
    }

    fun writeFloat(float: Float) {
        this.bytes.addAll(float.toByteArrayBE().toList())
    }

    fun writeFloatLE(float: Float) {
        this.bytes.addAll(float.toByteArrayLE().toList())
    }

    fun writeDouble(double: Double) {
        this.bytes.addAll(double.toByteArrayBE().toList())
    }

    fun writeDoubleLE(double: Double) {
        this.bytes.addAll(double.toByteArrayLE().toList())
    }

    fun writeUByte(byte: UByte) {
        this.bytes.add(byte.toByte())
    }

    fun writeUShort(uShort: UShort) {
        this.bytes.addAll(uShort.toByteArrayBE().toList())
    }

    fun writeUShortLE(uShort: UShort) {
        this.bytes.addAll(uShort.toByteArrayLE().toList())
    }

    fun writeUInt(uInt: UInt) {
        this.bytes.addAll(uInt.toByteArrayBE().toList())
    }

    fun writeUIntLE(uInt: UInt) {
        this.bytes.addAll(uInt.toByteArrayLE().toList())
    }

    fun writeULong(uLong: ULong) {
        this.bytes.addAll(uLong.toByteArrayBE().toList())
    }

    fun writeULongLE(uLong: ULong) {
        this.bytes.addAll(uLong.toByteArrayLE().toList())
    }

    fun writeBytes(bytes: ByteArray) {
        this.bytes.addAll(bytes.toList())
    }

    fun writeBytes(byteBuf: ByteBuf) {
        (0 until byteBuf.readableBytes()).map { byteBuf.readByte() }.forEach { this.bytes.add(it) }
    }

    fun writeLengthenedString(string: String, charset: Charset = Charsets.UTF_8) {
        val bytes = string.toByteArray(charset)
        this.writeInt(bytes.size)
        this.writeBytes(bytes)
    }

    fun writeString(string: String, charset: Charset = Charsets.UTF_8) {
        this.writeBytes(string.toByteArray(charset))
    }

    fun writeVarInt(value: Int): Int {
        var input = value
        var length = 1
        while (true) {
            if ((input and SEGMENT_BITS.inv()) == 0) {
                this.writeByte(input.toByte())
                length++
                return length
            }

            this.writeByte(((input and SEGMENT_BITS) or CONTINUE_BIT).toByte())
            input = input ushr 7
            length++
        }
    }

    fun writeUUID(uuid: UUID) {
        this.writeLong(uuid.mostSignificantBits)
        this.writeLong(uuid.leastSignificantBits)
    }

    fun readBoolean(): Boolean {
        return this.readByte() != 0.toByte()
    }

    fun readByte(): Byte {
        if (readerIndex >= bytes.size) {
            throw ArrayIndexOutOfBoundsException()
        }
        return bytes[readerIndex].apply { readerIndex++ }
    }

    fun readShort(): Short {
        val byteArray = byteArrayOf(
            readByte(),
            readByte()
        )
        return byteArray.toShortBE()
    }

    fun readShortLE(): Short {
        val byteArray = byteArrayOf(
            readByte(),
            readByte()
        )
        return byteArray.toShortLE()
    }

    fun readInt(): Int {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte()
        )
        return byteArray.toIntBE()
    }

    fun readIntLE(): Int {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte()
        )
        return byteArray.toIntLE()
    }

    fun readLong(): Long {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte()
        )
        return byteArray.toLongBE()
    }

    fun readLongLE(): Long {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte()
        )
        return byteArray.toLongLE()
    }

    fun readFloat(): Float {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte(),
        )
        return byteArray.toFloatBE()
    }

    fun readFloatLE(): Float {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte(),
        )
        return byteArray.toFloatLE()
    }

    fun readDouble(): Double {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
        )
        return byteArray.toDoubleBE()
    }

    fun readDoubleLE(): Double {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
        )
        return byteArray.toDoubleLE()
    }

    fun readUByte(): UByte {
        return readByte().toUByte()
    }

    fun readUShort(): UShort {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
        )
        return byteArray.toUShortBE()
    }

    fun readUShortLE(): UShort {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
        )
        return byteArray.toUShortLE()
    }

    fun readUInt(): UInt {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte()
        )
        return byteArray.toUIntBE()
    }

    fun readUIntLE(): UInt {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte()
        )
        return byteArray.toUIntLE()
    }

    fun readULong(): ULong {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte()
        )
        return byteArray.toULongBE()
    }

    fun readULongLE(): ULong {
        val byteArray = byteArrayOf(
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte(),
            readByte()
        )
        return byteArray.toULongLE()
    }

    fun readBytes(length: Int): ByteArray {
        return (0 until length).map { readByte() }.toByteArray()
    }

    fun readLengthenedString(charset: Charset): String {
        val length = this.readInt()
        return this.readString(length, charset)
    }

    fun readString(length: Int, charset: Charset): String {
        return this.readBytes(length).toString(charset)
    }

    fun readVarInt(): Int {
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

    fun readUUID(): UUID {
        return UUID(this.readLong(), this.readLong())
    }

    fun readableBytes(): Int {
        return this.bytes.size - readerIndex
    }

}