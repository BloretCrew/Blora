package blora.path.path2d

interface PathPoint2D {

    val x: Int
    val y: Int

    companion object {

        fun of(x: Int, y: Int): PathPoint2D {
            return InternalPathPoint2D(x, y)
        }

    }

}

internal data class InternalPathPoint2D(
    override val x: Int,
    override val y: Int,
    val g: Int = 0,
    val h: Int = 0
) : PathPoint2D, Comparable<InternalPathPoint2D> {

    val f: Int
        get() = g + h

    override fun compareTo(other: InternalPathPoint2D): Int {
        return this.f.compareTo(other.f).takeIf { it != 0 } ?: this.h.compareTo(other.h)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (other !is PathPoint2D)
            return false
        return x == other.x && y == other.y
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

}