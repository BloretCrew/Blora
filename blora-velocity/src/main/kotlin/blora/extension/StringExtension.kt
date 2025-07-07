package blora.extension

val PASSWORD_CHARS = "!@#$%^&*()_+-={}[]:;,.<>/?\\|"

fun Appendable.appendNbtString(value: String, forceQuote: Boolean = false): Appendable {
    fun Appendable.appendQuoted(): Appendable = apply {
        append('"')
        value.forEach {
            if (it == '"') append("\\\"") else append(it)
        }
        append('"')
    }

    fun Char.isSafeCharacter(): Boolean = when (this) {
        '-', '_', in 'a'..'z', in 'A'..'Z', in '0'..'9' -> true
        else -> false
    }

    return when {
        forceQuote -> appendQuoted()
        value.isEmpty() -> append("\"\"")
        value.all { it.isSafeCharacter() } -> append(value)
        !value.contains('"') -> append('"').append(value).append('"')
        !value.contains('\'') -> append('\'').append(value).append('\'')
        else -> appendQuoted()
    }
}

fun String.hasRepeatedCharRegex(n: Int): Boolean {
    if (n <= 1)
        return false
    if (n > this.length)
        return true
    val pattern = "(.)\\1{${n - 1}}".toRegex()
    return pattern.containsMatchIn(this)
}

fun String.containsIllegalCharacters(): Boolean {
    for (char in this) {
        if (!char.isLetterOrDigit() && !PASSWORD_CHARS.contains(char))
            return true
    }
    return false
}

fun String.containsUppercase(): Boolean {
    for (char in this) {
        if (char.isUpperCase())
            return true
    }
    return false
}

fun String.containsNumber(): Boolean {
    for (char in this) {
        if (char.isDigit())
            return true
    }
    return false
}

fun String.containsSymbol(): Boolean {
    for (char in this) {
        if (PASSWORD_CHARS.contains(char)) {
            return true
        }
    }
    return false
}

fun String.hasConsecutiveSequence(minimum: Int = 3): Boolean {
    if (minimum < 3)
        return false
    if (this.length < minimum)
        return false

    for (i in 0..this.length - 4) {
        val (type, valid) = getCharType(this[i])
        if (!valid) continue

        if ((i + 3 < this.length) &&
            (1..3).all { j -> isSameType(type, this[i + j]) }
        ) {

            val asc = this[i] + 1 == this[i + 1] &&
                    this[i + 1] + 1 == this[i + 2] &&
                    this[i + 2] + 1 == this[i + 3]

            val desc = this[i] - 1 == this[i + 1] &&
                    this[i + 1] - 1 == this[i + 2] &&
                    this[i + 2] - 1 == this[i + 3]

            if (asc || desc) return true
        }
    }
    return false
}

fun String.convertCamelCase(
    delimiter: Char
): String {
    if (isEmpty())
        return this

    return buildString(length * 2) {
        append(this@convertCamelCase[0].lowercaseChar())

        for (i in 1 until this@convertCamelCase.length) {
            val c = this@convertCamelCase[i]
            val prev = this@convertCamelCase[i - 1]

            if (c.isUpperCase()) {
                if (!prev.isWhitespace()) {
                    append(delimiter)
                }
                append(c.lowercaseChar())
            } else {
                append(c)
            }
        }
    }
}

private fun getCharType(c: Char): Pair<Int, Boolean> {
    return when {
        c in '0'..'9' -> Pair(1, true)   // 数字类型
        c in 'a'..'z' -> Pair(2, true)   // 小写字母
        c in 'A'..'Z' -> Pair(3, true)   // 大写字母
        else -> Pair(0, false)           // 其他字符
    }
}

private fun isSameType(baseType: Int, c: Char): Boolean {
    return when (baseType) {
        1 -> c in '0'..'9'
        2 -> c in 'a'..'z'
        3 -> c in 'A'..'Z'
        else -> false
    }
}