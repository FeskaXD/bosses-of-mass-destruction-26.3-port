package net.barribob.boss.mob.ai.valid_direction

import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.ClipContext

class CanMoveThrough(private val entity: Entity, private val reactionDistance: Double) : IValidDirection {
    override fun isValidDirection(normedDirection: Vec3): Boolean {
        val reactionDirection = normedDirection.scale(reactionDistance).add(entity.deltaMovement)
        val target = entity.position().add(reactionDirection)
        val noBlockCollisions = MathUtils.willBoxFit(entity.boundingBox, reactionDirection) { !entity.level().noCollision(entity, it) }
        val blockCollision = entity.level().clip(
            ClipContext(
                entity.position().add(normedDirection.scale(1.0)),
                target,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                entity
            )
        )
        val noFluidCollisions = blockCollision.type == HitResult.Type.MISS

        return noFluidCollisions && noBlockCollisions
    }
}