package blora.extension

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

fun String.containsLetterAndNumberOnly(): Boolean {
    for (char in this) {
        if (!char.isLetterOrDigit()) {
            return false
        }
    }
    return true
}

fun String.containsNumberOnly(): Boolean {
    if (this.contains(".") && this.count { it == '.' } > 1) {
        return false
    }

    for (char in this.replace(".", "").substring(if (this.startsWith("-")) 1 else 0)) {
        if (!char.isDigit()) {
            return false
        }
    }
    return true
}

fun String.containsLowercaseLetterOnly(): Boolean {
    for (char in this) {
        if (!char.isLetter() || !char.isLowerCase()) {
            return false
        }
    }
    return true
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