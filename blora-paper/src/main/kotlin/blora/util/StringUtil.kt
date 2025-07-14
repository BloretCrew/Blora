package blora.util

import kotlin.random.Random

fun randomString(
    length: Int,
    chars: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
): String {
    val builder = StringBuilder()
    for (i in 0 until length) {
        builder.append(chars[Random.nextInt(chars.length)])
    }
    return builder.toString()
}