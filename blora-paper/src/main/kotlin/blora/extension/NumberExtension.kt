package blora.extension

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun Short.toByteArrayBE(): ByteArray = ByteBuffer.allocate(2).apply {
    order(ByteOrder.BIG_ENDIAN)
    putShort(this@toByteArrayBE)
}.array()

fun Short.toByteArrayLE(): ByteArray = ByteBuffer.allocate(2).apply {
    order(ByteOrder.LITTLE_ENDIAN)
    putShort(this@toByteArrayLE)
}.array()

fun Int.toByteArrayBE(): ByteArray = ByteBuffer.allocate(4).apply {
    order(ByteOrder.BIG_ENDIAN)
    putInt(this@toByteArrayBE)
}.array()

fun Int.toByteArrayLE(): ByteArray = ByteBuffer.allocate(4).apply {
    order(ByteOrder.LITTLE_ENDIAN)
    putInt(this@toByteArrayLE)
}.array()

fun Long.toByteArrayBE(): ByteArray = ByteBuffer.allocate(8).apply {
    order(ByteOrder.BIG_ENDIAN)
    putLong(this@toByteArrayBE)
}.array()

fun Long.toByteArrayLE(): ByteArray = ByteBuffer.allocate(8).apply {
    order(ByteOrder.LITTLE_ENDIAN)
    putLong(this@toByteArrayLE)
}.array()

fun Float.toByteArrayBE(): ByteArray = ByteBuffer.allocate(4).apply {
    order(ByteOrder.BIG_ENDIAN)
    putFloat(this@toByteArrayBE)
}.array()

fun Float.toByteArrayLE(): ByteArray = ByteBuffer.allocate(4).apply {
    order(ByteOrder.LITTLE_ENDIAN)
    putFloat(this@toByteArrayLE)
}.array()

fun Double.toByteArrayBE(): ByteArray = ByteBuffer.allocate(8).apply {
    order(ByteOrder.BIG_ENDIAN)
    putDouble(this@toByteArrayBE)
}.array()

fun Double.toByteArrayLE(): ByteArray = ByteBuffer.allocate(8).apply {
    order(ByteOrder.LITTLE_ENDIAN)
    putDouble(this@toByteArrayLE)
}.array()

fun UShort.toByteArrayBE(): ByteArray = ByteBuffer.allocate(2).apply {
    order(ByteOrder.BIG_ENDIAN)
    putShort(this@toByteArrayBE.toShort())
}.array()

fun UShort.toByteArrayLE(): ByteArray = ByteBuffer.allocate(2).apply {
    order(ByteOrder.LITTLE_ENDIAN)
    putShort(this@toByteArrayLE.toShort())
}.array()

fun UInt.toByteArrayBE(): ByteArray = ByteBuffer.allocate(4).apply {
    order(ByteOrder.BIG_ENDIAN)
    putInt(this@toByteArrayBE.toInt())
}.array()

fun UInt.toByteArrayLE(): ByteArray = ByteBuffer.allocate(4).apply {
    order(ByteOrder.LITTLE_ENDIAN)
    putInt(this@toByteArrayLE.toInt())
}.array()

fun ULong.toByteArrayBE(): ByteArray = ByteBuffer.allocate(8).apply {
    order(ByteOrder.BIG_ENDIAN)
    putLong(this@toByteArrayBE.toLong())
}.array()

fun ULong.toByteArrayLE(): ByteArray = ByteBuffer.allocate(8).apply {
    order(ByteOrder.LITTLE_ENDIAN)
    putLong(this@toByteArrayLE.toLong())
}.array()