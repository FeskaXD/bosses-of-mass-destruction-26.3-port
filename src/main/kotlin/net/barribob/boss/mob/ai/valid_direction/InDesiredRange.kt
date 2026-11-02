package net.barribob.boss.mob.ai.valid_direction

import net.minecraft.world.phys.Vec3

class InDesiredRange(
    val tooClose: (Vec3) -> Boolean,
    val tooFar: (Vec3) -> Boolean,
    val movingCloser: (Vec3) -> Boolean
) : IValidDirection {
    override fun isValidDirection(normedDirection: Vec3): Boolean {
        val isTooClose = tooClose(normedDirection)
        val isMovingCloser = movingCloser(normedDirection)
        val isTooFar = tooFar(normedDirection)

        return (isTooClose && !isMovingCloser) ||
                (isTooFar && isMovingCloser) ||
                (!isTooFar && !isTooClose)
    }
}