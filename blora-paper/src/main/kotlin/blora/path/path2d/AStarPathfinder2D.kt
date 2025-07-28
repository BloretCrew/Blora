package blora.path.path2d

import java.util.*
import kotlin.math.abs

object AStarPathfinder2D : Pathfinder2D {

    override fun findPath(start: PathPoint2D, end: PathPoint2D, filter: PathfinderFilter2D): Path2D? {
        if (!filter.passable(start) || !filter.passable(end))
            return null

        val directions = listOf(
            InternalPathPoint2D(0, 1),
            InternalPathPoint2D(1, 0),
            InternalPathPoint2D(0, -1),
            InternalPathPoint2D(-1, 0),
        )

        val gValues = mutableMapOf<PathPoint2D, Int>().apply {
            put(start, 0)
        }

        val openSet = PriorityQueue<PathPoint2D>().apply {
            this.add(
                InternalPathPoint2D(
                    start.x, start.y, h = manhattanDistance(
                        start.x,
                        start.y,
                        end.x,
                        end.y
                    )
                )
            )
        }

        val cameFrom = mutableMapOf<PathPoint2D, PathPoint2D>()

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()

            if (current.x == end.x && current.y == end.y) {
                return InternalPath2D(reconstructPath(cameFrom, current))
            }

            for ((dx, dy) in directions) {
                val nextX = current.x + dx
                val nextY = current.y + dy
                val nextPos = PathPoint2D.of(nextX, nextY)

                if (!filter.passable(PathPoint2D.of(nextX, nextY))) continue

                val tentativeG = (current as InternalPathPoint2D).g + 1

                if (tentativeG < gValues.getOrDefault(nextPos, Int.MAX_VALUE)) {
                    cameFrom[nextPos] = current
                    gValues[nextPos] = tentativeG

                    val nextNode = InternalPathPoint2D(
                        x = nextX,
                        y = nextY,
                        g = tentativeG,
                        h = manhattanDistance(nextX, nextY, end.x, end.y)
                    )

                    openSet.add(nextNode)
                }
            }
        }

        return null
    }

    private fun manhattanDistance(x1: Int, y1: Int, x2: Int, y2: Int): Int =
        abs(x1 - x2) + abs(y1 - y2)

    private fun reconstructPath(
        cameFrom: Map<PathPoint2D, PathPoint2D>,
        endPos: PathPoint2D
    ): List<PathPoint2D> {
        val path = mutableListOf(endPos)
        var current = endPos

        while (cameFrom.containsKey(current)) {
            current = cameFrom[current]!!
            path.add(0, current)
        }

        return path
    }

}