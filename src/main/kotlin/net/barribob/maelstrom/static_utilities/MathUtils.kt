package net.barribob.maelstrom.static_utilities

import net.barribob.maelstrom.general.math.ReferencedAxisRotator
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import kotlin.math.*

fun AABB.corners() = listOf(
    Vec3(this.minX, this.maxY, this.minZ),
    Vec3(this.maxX, this.maxY, this.minZ),
    Vec3(this.minX, this.maxY, this.maxZ),
    Vec3(this.maxX, this.maxY, this.maxZ),
    Vec3(this.minX, this.minY, this.minZ),
    Vec3(this.maxX, this.minY, this.minZ),
    Vec3(this.minX, this.minY, this.maxZ),
    Vec3(this.maxX, this.minY, this.maxZ)
)

// https://stackoverflow.com/questions/23086291/format-in-kotlin-string-templates
fun Double.format(digits: Int) = "%.${digits}f".format(this)

object MathUtils {
    fun withinDistance(pos1: Vec3, pos2: Vec3, distance: Double): Boolean {
        if (distance < 0) throw IllegalArgumentException("Distance cannot be negative")
        return pos1.distanceToSqr(pos2) < distance.pow(2.0)
    }

    fun movingTowards(center: Vec3, pos: Vec3, direction: Vec3): Boolean {
        val directionTo = unNormedDirection(pos, center)
        return direction.dot(directionTo) > 0
    }

    fun unNormedDirection(source: Vec3, target: Vec3): Vec3 = target.subtract(source)

    /**
     * Calls a function that linearly interpolates between two points. Includes both ends of the line
     *
     * Callback returns the position and the point number from 1 to points
     */
    fun lineCallback(start: Vec3, end: Vec3, points: Int, callback: (Vec3, Int) -> Unit) {
        val dir: Vec3 = end.subtract(start).scale(1 / (points - 1).toDouble())
        var pos = start
        for (i in 0 until points) {
            callback(pos, i)
            pos = pos.add(dir)
        }
    }

    fun circleCallback(radius: Double, points: Int, axis: Vec3, callback: (Vec3) -> Unit) {
        val degrees = Math.PI * 2 / points
        val axisYaw = directionToYaw(axis)
        val rotator = ReferencedAxisRotator(VecUtils.yAxis, axis)
        for (i in 0 until points) {
            val radians = i * degrees
            val offset = Vec3(sin(radians), 0.0, cos(radians))
                .scale(radius)
                .rotateVector(VecUtils.yAxis, -axisYaw)
            val rotated = rotator.rotate(offset)
            callback(rotated)
        }
    }

    fun circlePoints(radius: Double, points: Int, axis: Vec3): Collection<Vec3> {
        val vectors = mutableListOf<Vec3>()
        circleCallback(radius, points, axis, vectors::add)
        return vectors
    }

    fun willBoxFit(box: AABB, movement: Vec3, collision: (AABB) -> Boolean): Boolean {
        var collided = false
        val points = ceil(movement.length() / box.size).toInt()
        lineCallback(Vec3.ZERO, movement, points) { vec3d, _ ->
            if (collision(box.move(vec3d))) {
                collided = true
            }
        }

        return !collided
    }

    fun directionToPitch(direction: Vec3): Float {
        val x: Double = direction.x
        val z: Double = direction.z
        val y: Double = direction.y

        val h = sqrt(x * x + z * z)
        return (Math.toDegrees(-(Mth.atan2(y, h)))).toFloat()
    }

    fun directionToYaw(direction: Vec3): Double {
        val x: Double = direction.x
        val z: Double = direction.z

        return Math.toDegrees(Mth.atan2(z, x))
    }

    fun lerpVec(partialTicks: Float, vec1: Vec3, vec2: Vec3): Vec3 {
        val pt = partialTicks.toDouble()
        val x = Mth.lerp(pt, vec1.x, vec2.x)
        val y = Mth.lerp(pt, vec1.y, vec2.y)
        val z = Mth.lerp(pt, vec1.z, vec2.z)
        return Vec3(x, y, z)
    }

    fun axisOffset(direction: Vec3, offset: Vec3): Vec3 {
        val forward: Vec3 = direction.normalize()
        val side: Vec3 = forward.cross(VecUtils.yAxis).normalize()
        val up: Vec3 = side.cross(forward).normalize()
        return forward.scale(offset.x).add(side.scale(offset.z)).add(up.scale(offset.y))
    }

    fun facingSameDirection(direction1: Vec3, direction2: Vec3): Boolean {
        return direction1.dot(direction2) > 0
    }

    // https://www.wikihow.com/Add-Consecutive-Integers-from-1-to-100
    fun consecutiveSum(firstNumber: Int, lastNumber: Int): Int {
        return ((lastNumber - firstNumber + 1) * ((firstNumber + lastNumber) * 0.5f)).toInt()
    }

    fun roundedStep(n: Float, steps: List<Float>, floor: Boolean = false): Float {
        return if (floor) {
            steps.sortedDescending().firstOrNull { it <= n } ?: steps.first()
        } else {
            steps.sorted().firstOrNull { it > n } ?: steps.last()
        }
    }

    fun buildBlockCircle(radius: Double): List<Vec3> {
        val intRadius = radius.toInt()
        val radiusSq = radius * radius
        val points = mutableListOf<Vec3>()
        for (x in -intRadius..intRadius) {
            for (z in -intRadius..intRadius) {
                val pos = Vec3(x.toDouble(), 0.0, z.toDouble())
                if (pos.lengthSqr() <= radiusSq) {
                    points.add(pos)
                }
            }
        }
        return points
    }

    // https://www.geeksforgeeks.org/bresenhams-algorithm-for-3-d-line-drawing/
    fun getBlocksInLine(startPos: BlockPos, endPos: BlockPos) : List<BlockPos> {
        var x1 = startPos.x
        var y1 = startPos.y
        var z1 = startPos.z
        val x2 = endPos.x
        val y2 = endPos.y
        val z2 = endPos.z
        val points = mutableListOf(startPos)
        val dx = abs(x2 - x1)
        val dy = abs(y2 - y1)
        val dz = abs(z2 - z1)
        val xs = if (x2 > x1) 1 else -1
        val ys = if (y2 > y1) 1 else -1
        val zs = if (z2 > z1) 1 else -1

        if (dx >= dy && dx >= dz) {
            var p1 = 2 * dy - dx
            var p2 = 2 * dz - dx
            while (x1 != x2) {
                x1 += xs
                if (p1 >= 0) {
                    y1 += ys
                    p1 -= 2 * dx
                }
                if (p2 >= 0) {
                    z1 += zs
                    p2 -= 2 * dx
                }
                p1 += 2 * dy
                p2 += 2 * dz
                points.add(BlockPos(x1, y1, z1))
            }
        }

        else if (dy >= dx && dy >= dz) {
            var p1 = 2 * dx - dy
            var p2 = 2 * dz - dy
            while (y1 != y2) {
                y1 += ys
                if (p1 >= 0) {
                    x1 += xs
                    p1 -= 2 * dy
                }
                if (p2 >= 0) {
                    z1 += zs
                    p2 -= 2 * dy
                }
                p1 += 2 * dx
                p2 += 2 * dz
                points.add(BlockPos(x1, y1, z1))
            }
        }

        else {
            var p1 = 2 * dy - dz
            var p2 = 2 * dx - dz
            while (z1 != z2) {
                z1 += zs
                if (p1 >= 0) {
                    y1 += ys
                    p1 -= 2 * dz
                }
                if (p2 >= 0) {
                    x1 += xs
                    p2 -= 2 * dz
                }
                p1 += 2 * dy
                p2 += 2 * dx
                points.add(BlockPos(x1, y1, z1))
            }
        }
        return points
    }

    fun ratioLerp(time: Float, ratio: Float, maxAge: Float, partialTicks: Float): Float {
        assert(ratio <= 1)
        assert(ratio >= 0)
        assert(maxAge > 0)

        val currentTime = Mth.clamp((time + partialTicks) / maxAge, 0f, 1f)
        return max(0f, currentTime - ratio) / (1 - ratio)
    }
}