package blora.path.path2d

fun interface PathfinderFilter2D {

    fun passable(pathPoint: PathPoint2D): Boolean

}