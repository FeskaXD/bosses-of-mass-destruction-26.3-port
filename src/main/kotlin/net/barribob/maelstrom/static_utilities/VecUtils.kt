package net.barribob.maelstrom.static_utilities

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

fun Vec3.yOffset(i: Double): Vec3 = this.add(0.0, i, 0.0)

fun Vec3.planeProject(planeVector: Vec3): Vec3 = this.subtract(planeVector.scale(this.dot(planeVector)))

fun newVec3d(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): Vec3 = Vec3(x, y, z)

fun BlockPos.asVec3d(): Vec3 = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())

fun Vec3.negateServer(): Vec3 = this.scale(-1.0) // Why do you force me to do this mojang

fun Vec3.coerceAtLeast(vec: Vec3): Vec3 = Vec3(x.coerceAtLeast(vec.x), y.coerceAtLeast(vec.y), z.coerceAtLeast(vec.z))

fun Vec3.coerceAtMost(vec: Vec3): Vec3 = Vec3(x.coerceAtMost(vec.x), y.coerceAtMost(vec.y), z.coerceAtMost(vec.z))

// http://www.java-gaming.org/index.php/topic,28253
// https://www.omnicalculator.com/math/angle-between-two-vectors#angle-between-two-vectors-formulas
fun Vec3.unsignedAngle(b: Vec3): Double {
    val dot = this.dot(b)
    val lengths = this.length() * b.length()

    if (lengths == 0.0) {
        return 0.0
    }

    val cos: Double = (dot / lengths)
        .coerceAtLeast(-1.0)
        .coerceAtMost(1.0)
    return Math.toDegrees(acos(cos))
}

/**
 * Rotate a vector around an axis by given degrees https://stackoverflow.com/questions/31225062/rotating-a-vector-by-angle-and-axis-in-java
 */
fun Vec3.rotateVector(axis: Vec3, degrees: Double): Vec3 {
    val theta = Math.toRadians(degrees)
    val normedAxis = axis.normalize()
    val x: Double = this.x
    val y: Double = this.y
    val z: Double = this.z
    val u: Double = normedAxis.x
    val v: Double = normedAxis.y
    val w: Double = normedAxis.z
    val xPrime = u * (u * x + v * y + w * z) * (1.0 - cos(theta)) + x * cos(theta) + (-w * y + v * z) * sin(theta)
    val yPrime = v * (u * x + v * y + w * z) * (1.0 - cos(theta)) + y * cos(theta) + (w * x - u * z) * sin(theta)
    val zPrime = w * (u * x + v * y + w * z) * (1.0 - cos(theta)) + z * cos(theta) + (-v * x + u * y) * sin(theta)
    return Vec3(xPrime, yPrime, zPrime)
}

object VecUtils {
    val xAxis = newVec3d(1.0)
    val yAxis = newVec3d(y = 1.0)
    val zAxis = newVec3d(z = 1.0)
    val unit = Vec3(1.0, 1.0, 1.0)
}
