package blora.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Password storage:
 * - New: `$pbkdf2-sha256$iterations$salt$hash` in hashedPassword1;
 *   hashedPassword2/3 = [MARKER]
 * - Legacy: triple (md5, sha256, sha512) from [PasswordHasher]
 */
object SecurePasswordHasher {

    const val MARKER = "%pbkdf2%"
    const val UNREGISTERED = "%unregistered%"

    private const val ITERATIONS = 210_000
    private const val KEY_LENGTH_BITS = 256
    private val random = SecureRandom()

    fun isUnregistered(p1: String, p2: String, p3: String): Boolean {
        return p1 == UNREGISTERED && p2 == UNREGISTERED && p3 == UNREGISTERED
    }

    fun isNewFormat(p2: String, p3: String): Boolean {
        return p2 == MARKER && p3 == MARKER
    }

    fun hashNew(password: String): Triple<String, String, String> {
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val hash = pbkdf2(password.toCharArray(), salt, ITERATIONS)
        val encoded =
            "\$pbkdf2-sha256\$$ITERATIONS\$${b64(salt)}\$${b64(hash)}"
        return Triple(encoded, MARKER, MARKER)
    }

    fun verify(password: String, p1: String, p2: String, p3: String): Boolean {
        if (isUnregistered(p1, p2, p3)) {
            return false
        }
        if (isNewFormat(p2, p3)) {
            return verifyPbkdf2(password, p1)
        }
        // Legacy triple hash
        return PasswordHasher.hash(password) == Triple(p1, p2, p3)
    }

    /** True if legacy hash and should be upgraded after successful login. */
    fun needsUpgrade(p2: String, p3: String): Boolean {
        return !isNewFormat(p2, p3) && p2 != UNREGISTERED
    }

    private fun verifyPbkdf2(password: String, encoded: String): Boolean {
        // $pbkdf2-sha256$iterations$salt$hash
        val parts = encoded.split('$').filter { it.isNotEmpty() }
        if (parts.size != 4 || parts[0] != "pbkdf2-sha256") {
            return false
        }
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = runCatching { Base64.getDecoder().decode(parts[2]) }.getOrNull() ?: return false
        val expected = runCatching { Base64.getDecoder().decode(parts[3]) }.getOrNull() ?: return false
        val actual = pbkdf2(password.toCharArray(), salt, iterations)
        return MessageDigest.isEqual(expected, actual)
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun b64(bytes: ByteArray): String =
        Base64.getEncoder().withoutPadding().encodeToString(bytes)

}
