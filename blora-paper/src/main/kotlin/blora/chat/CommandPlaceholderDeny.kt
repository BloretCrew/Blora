package blora.chat

import blora.plugin.BloraPlugin

/**
 * Checks whether a chat `<cmd:...>` suggestion is blocked by config deny list.
 */
object CommandPlaceholderDeny {

    fun isDenied(command: String): Boolean {
        val denyList = BloraPlugin.configuration.chat.commandPlaceholderDenyList
        if (denyList.isEmpty()) {
            return false
        }
        val normalized = command.trim().removePrefix("/").lowercase()
        if (normalized.isEmpty()) {
            return true
        }
        val firstToken = normalized.split(Regex("\\s+"), limit = 2)[0]
        val baseName = firstToken.substringAfterLast(':')
        return denyList.any { raw ->
            val denied = raw.trim().removePrefix("/").lowercase()
            if (denied.isEmpty()) {
                return@any false
            }
            val deniedFirst = denied.split(Regex("\\s+"), limit = 2)[0]
            val deniedBase = deniedFirst.substringAfterLast(':')
            firstToken == deniedFirst ||
                baseName == deniedBase ||
                firstToken == deniedBase ||
                normalized == denied ||
                normalized.startsWith("$denied ")
        }
    }

}
