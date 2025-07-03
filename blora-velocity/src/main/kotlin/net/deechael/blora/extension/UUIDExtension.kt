package net.deechael.blora.extension

import java.math.BigInteger
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Uuid.Companion.fromUndashedString(id: String): UUID {
    return UUID(
        BigInteger(id.substring(0, 16), 16).toLong(),
        BigInteger(id.substring(16, 32), 16).toLong()
    )
}