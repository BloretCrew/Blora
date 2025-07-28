package blora.path

fun interface PathfinderFilter {

    fun passable(pathPoint: PathPoint): Boolean

}