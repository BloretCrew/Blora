package blora.authorization

import com.velocitypowered.api.proxy.Player
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Server-side auth dialog state. Clients can send arbitrary CustomClickAction IDs;
 * callbacks must only proceed when the server has opened the matching dialog.
 */
enum class AuthDialogKind {
    NONE,
    EULA,
    LOGIN,
    REGISTER,
    CHANGE_PASSWORD,
}

data class AuthFlowSession(
    val kind: AuthDialogKind,
    val openedAtMs: Long = System.currentTimeMillis(),
)

object AuthFlow {

    private val sessions = ConcurrentHashMap<UUID, AuthFlowSession>()
    private val secureRandom = SecureRandom()

    /** Dialog validity window (ms). */
    private const val TTL_MS = 10 * 60 * 1000L

    fun open(player: Player, kind: AuthDialogKind) {
        sessions[player.uniqueId] = AuthFlowSession(kind)
    }

    /**
     * Validates and consumes the dialog session (one-shot).
     * Returns false if missing, wrong kind, expired, or already consumed.
     */
    fun consume(player: Player, expected: AuthDialogKind): Boolean {
        val session = sessions.remove(player.uniqueId) ?: return false
        if (session.kind != expected) {
            // Put back only if we removed a different kind by mistake — actually we already
            // removed; wrong kind means reject and do not restore (forces re-open).
            return false
        }
        if (System.currentTimeMillis() - session.openedAtMs > TTL_MS) {
            return false
        }
        return true
    }

    /** Peek without consuming (e.g. soft checks). */
    fun current(player: Player): AuthDialogKind {
        val session = sessions[player.uniqueId] ?: return AuthDialogKind.NONE
        if (System.currentTimeMillis() - session.openedAtMs > TTL_MS) {
            sessions.remove(player.uniqueId)
            return AuthDialogKind.NONE
        }
        return session.kind
    }

    fun clear(player: Player) {
        sessions.remove(player.uniqueId)
    }

    fun newNonce(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

}
