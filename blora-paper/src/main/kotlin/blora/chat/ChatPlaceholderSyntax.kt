package blora.chat

import blora.plugin.BloraPlugin

/**
 * Accepts square-bracket aliases for chat placeholders while keeping the existing
 * angle-bracket parser as the single source of truth.
 */
object ChatPlaceholderSyntax {

    private val aliases = mapOf(
        "i" to "item",
        "v" to "inv",
        "e" to "enderchest",
    )

    private val reservedCustomNames = aliases.keys
    private val builtInNames = setOf("item", "inv", "enderchest")
    private val argumentNames = setOf("cmd", "link", "copy")
    private const val PAPI_ARGUMENT = "papi"

    /**
     * Converts supported square placeholders to the existing angle syntax. A square group
     * that is not itself a valid placeholder is treated as ordinary text, so valid inner
     * placeholders behave like native MiniMessage tags inside extra or malformed `<` text.
     */
    fun normalize(message: String, allowPapi: Boolean): String {
        return normalize(message, allowPapi, supportedCustomNames())
    }

    internal fun normalize(
        message: String,
        allowPapi: Boolean,
        customNames: Set<String>,
    ): String {
        if ('[' !in message) {
            return message
        }

        val normalizedMessage = StringBuilder(message.length)
        var index = 0
        while (index < message.length) {
            when (message[index]) {
                '<' -> {
                    if (!isPotentialAngleTagStart(message, index)) {
                        normalizedMessage.append(message[index])
                        index++
                        continue
                    }
                    val angleEnd = findAngleTagEnd(message, index)
                    if (angleEnd == -1) {
                        normalizedMessage.append(message[index])
                        index++
                    } else {
                        normalizedMessage.append(message, index, angleEnd + 1)
                        index = angleEnd + 1
                    }
                }

                '[' -> {
                    val bracketEnd = findMatchingBracketEnd(message, index)
                    if (bracketEnd == -1) {
                        normalizedMessage.append(message[index])
                        index++
                        continue
                    }

                    val content = message.substring(index + 1, bracketEnd)
                    val normalized = aliases[content] ?: content
                    if (isSupported(normalized, allowPapi, customNames) &&
                        hasOnlyQuotedAngleDelimiters(content) &&
                        isValid(normalized)
                    ) {
                        normalizedMessage.append('<').append(normalized).append('>')
                        index = bracketEnd + 1
                    } else {
                        // The current `[` is ordinary text. Continue scanning so a later valid
                        // inner group can still normalize, matching `<<inv>>` MiniMessage behavior.
                        normalizedMessage.append(message[index])
                        index++
                    }
                }

                else -> {
                    normalizedMessage.append(message[index])
                    index++
                }
            }
        }
        return normalizedMessage.toString()
    }

    fun completions(allowPapi: Boolean): List<String> {
        return buildList {
            add("<item>")
            add("[item]")
            add("[i]")
            add("<inv>")
            add("[inv]")
            add("[v]")
            add("<enderchest>")
            add("[enderchest]")
            add("[e]")
            addArgumentCompletions("cmd")
            addArgumentCompletions("link")
            addArgumentCompletions("copy")
            if (allowPapi) {
                addArgumentCompletions(PAPI_ARGUMENT)
            }
            for (key in supportedCustomNames()) {
                add("<$key>")
                add("[$key]")
            }
        }.distinct()
    }

    fun isReservedCustomName(name: String): Boolean {
        return name in reservedCustomNames
    }

    fun supportedCustomPlaceholders(): Map<String, String> {
        return BloraPlugin.configuration.chat.placeholders.filterKeys { !isReservedCustomName(it) }
    }

    private fun supportedCustomNames(): Set<String> {
        return supportedCustomPlaceholders().keys
    }

    private fun isSupported(
        normalized: String,
        allowPapi: Boolean,
        customNames: Set<String>,
    ): Boolean {
        val name = normalized.substringBefore(':')
        return normalized in builtInNames ||
            (name in argumentNames && normalized.length > name.length + 1) ||
            (allowPapi && name == PAPI_ARGUMENT && normalized.length > name.length + 1) ||
            normalized in customNames
    }

    private fun isValid(normalized: String): Boolean {
        val name = normalized.substringBefore(':')
        val argument = normalized.substringAfter(':', missingDelimiterValue = "")
        return when (name) {
            "cmd" -> CommandTagResolver.parseAllowedCommand(argument) != null
            "link" -> LinkTagResolver.parseValidUrl(argument) != null
            else -> true
        }
    }

    private fun isPotentialAngleTagStart(message: String, start: Int): Boolean {
        val first = message.getOrNull(start + 1) ?: return false
        val nameStart = if (first == '/') message.getOrNull(start + 2) else first
        return nameStart != null && (nameStart.isLetterOrDigit() || nameStart == '_' || nameStart == '#')
    }

    private fun findAngleTagEnd(message: String, start: Int): Int {
        var quote: Char? = null
        for (index in start + 1 until message.length) {
            val current = message[index]
            if (quote != null) {
                if (current == quote) {
                    quote = null
                }
            } else {
                when {
                    current == '>' -> return index
                    current.isQuoteWithClosingPartner(message, index, '>') -> quote = current
                }
            }
        }
        return -1
    }

    private fun findMatchingBracketEnd(message: String, start: Int): Int {
        var quote: Char? = null
        var depth = 1
        for (index in start + 1 until message.length) {
            val current = message[index]
            if (quote != null) {
                if (current == quote) {
                    quote = null
                }
            } else {
                when {
                    current.isQuoteWithClosingPartner(message, index, ']') -> quote = current
                    current == '[' -> depth++
                    current == ']' -> {
                        depth--
                        if (depth == 0) {
                            return index
                        }
                    }
                }
            }
        }
        return -1
    }

    private fun hasOnlyQuotedAngleDelimiters(content: String): Boolean {
        var quote: Char? = null
        for (index in content.indices) {
            val current = content[index]
            if (quote != null) {
                if (current == quote) {
                    quote = null
                }
            } else {
                when {
                    current == '<' || current == '>' -> return false
                    current.isQuoteWithClosingPartner(content, index, null) -> quote = current
                }
            }
        }
        return true
    }

    private fun Char.isQuoteWithClosingPartner(text: String, index: Int, stopAt: Char?): Boolean {
        if (this != '\'' && this != '"') {
            return false
        }
        val mayContainDelimiter = index == 0 || text.getOrNull(index - 1) == ':'
        for (next in index + 1 until text.length) {
            val candidate = text[next]
            if (candidate == this) {
                return true
            }
            if (stopAt != null && candidate == stopAt) {
                if (!mayContainDelimiter || text.indexOf(this, next + 1) == -1) {
                    return false
                }
            }
        }
        return false
    }

    private fun MutableList<String>.addArgumentCompletions(name: String) {
        add("<$name:>")
        add("[$name:]")
    }
}
