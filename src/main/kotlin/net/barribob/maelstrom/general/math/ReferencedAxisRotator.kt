package net.barribob.maelstrom.general.math

import net.barribob.maelstrom.static_utilities.rotateVector
import net.barribob.maelstrom.static_utilities.unsignedAngle
import net.minecraft.world.phys.Vec3

class ReferencedAxisRotator(originalAxis: Vec3, newAxis: Vec3) {
    private val angleBetween = originalAxis.unsignedAngle(newAxis)
    private val rotationAxis: Vec3 = originalAxis.cross(newAxis)

    fun rotate(vec: Vec3): Vec3 {
        return vec.rotateVector(rotationAxis, angleBetween)
    }
}