package blora.formula

class StringReader(val input: String) {

    private var cursor = 0

    fun readUntilNotLetterOrDigitOrUnderline(): String {
        val stringBuilder = StringBuilder()
        while (this.readable() && (this.peek().isLetterOrDigit() || this.peek() == '_' || this.peek() == ':')) {
            stringBuilder.append(this.read())
        }
        return stringBuilder.toString()
    }

    fun skip() {
        this.cursor++
    }

    fun read(): Char {
        return this.input[cursor++]
    }

    fun peek(): Char {
        return this.input[cursor]
    }

    fun readable(): Boolean {
        return this.input.length > cursor
    }

}