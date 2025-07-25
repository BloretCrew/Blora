package blora.extension

fun Double.format(keep: Int = 2): String {
    if (keep <= 0)
        return this.toString()
    return "%.${keep}f".format(this)
}