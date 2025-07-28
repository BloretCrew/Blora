package blora.path.path2d

interface Pathfinder2D {

    fun findPath(start: PathPoint2D, end: PathPoint2D, filter: PathfinderFilter2D): Path2D?

}