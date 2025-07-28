package blora.path

interface Pathfinder {

    fun findPath(start: PathPoint, end: PathPoint, filter: PathfinderFilter): Path?

}