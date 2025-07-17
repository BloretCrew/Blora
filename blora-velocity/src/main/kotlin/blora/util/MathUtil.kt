package blora.util

fun positiveCeilDiv(x: Int, y: Int): Int {
    return -Math.floorDiv(-x, y)
}