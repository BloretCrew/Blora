package blora.path.path2d

interface Path2D {

    val points: List<PathPoint2D>

}

internal class InternalPath2D(
    override val points: List<PathPoint2D>
) : Path2D