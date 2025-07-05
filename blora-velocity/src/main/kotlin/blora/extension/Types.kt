package blora.extension

fun <T> T?.notNull(doNext: T.() -> Unit) {
    if (this != null) {
        this.doNext()
    }
}