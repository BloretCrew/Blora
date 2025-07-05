package blora.security

import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object PasswordHasher {

    fun hash(password: String): Triple<String, String, String> {
        val base64 = password.base64()
        val md5 = password.md5()
        val sha256 = password.sha256()
        val sha512 = password.sha512()

        var hasher = password + base64 + md5 + sha256 + sha512
        hasher = hasher + hasher.base64()
        hasher = hasher + hasher.md5()
        hasher = hasher + hasher.sha256()
        hasher = hasher + hasher.sha512()
        hasher = hasher.base64()

        return Triple(hasher.md5(), hasher.sha256(), hasher.sha512())
    }

}

@OptIn(ExperimentalEncodingApi::class)
internal fun String.base64(): String {
    return Base64.encode(this.toByteArray())
}

@OptIn(ExperimentalStdlibApi::class)
internal fun String.md5(): String {
    return MessageDigest.getInstance("MD5").digest(this.toByteArray()).toHexString()
}

@OptIn(ExperimentalStdlibApi::class)
internal fun String.sha256(): String {
    return MessageDigest.getInstance("SHA-256").digest(this.toByteArray()).toHexString()
}

@OptIn(ExperimentalStdlibApi::class)
internal fun String.sha512(): String {
    return MessageDigest.getInstance("SHA-512").digest(this.toByteArray()).toHexString()
}