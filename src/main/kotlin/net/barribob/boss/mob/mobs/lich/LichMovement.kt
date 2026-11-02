package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.mob.ai.ValidatedTargetSelector
import net.barribob.boss.mob.ai.VelocitySteering
import net.barribob.boss.mob.ai.goals.VelocityGoal
import net.barribob.boss.mob.ai.valid_direction.CanMoveThrough
import net.barribob.boss.mob.ai.valid_direction.InDesiredRange
import net.barribob.boss.mob.ai.valid_direction.ValidDirectionAnd
import net.barribob.boss.mob.utils.EntityAdapter
import net.barribob.boss.utils.VanillaCopies.lookAtTarget
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.newVec3d
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.phys.Vec3

class LichMovement(val entity: LichEntity) {
    private val reactionDistance = 4.0
    private val idleWanderDistance = 50.0
    private val iEntity = EntityAdapter(entity)
    private val tooFarFromTargetDistance = 30.0
    private val tooCloseToTargetDistance = 15.0

    fun buildAttackMovement(): VelocityGoal {
        val tooCloseToTarget: (Vec3) -> Boolean =
            getWithinDistancePredicate(tooCloseToTargetDistance) { entity.safeGetTargetPos() }
        val tooFarFromTarget: (Vec3) -> Boolean =
            { !getWithinDistancePredicate(tooFarFromTargetDistance) { entity.safeGetTargetPos()}(it) }
        val movingToTarget: (Vec3) -> Boolean = { MathUtils.movingTowards(entity.safeGetTargetPos(), entity.position(), it) }

        val canMoveTowardsPositionValidator = ValidDirectionAnd(
            listOf(
                CanMoveThrough(entity, reactionDistance),
                InDesiredRange(tooCloseToTarget, tooFarFromTarget, movingToTarget)
            )
        )
        val targetSelector = ValidatedTargetSelector(
            iEntity,
            canMoveTowardsPositionValidator,
            ModRandom()
        )
        return VelocityGoal(
            ::moveWhileAttacking,
            createSteering(),
            targetSelector
        )
    }

    private fun moveWhileAttacking(velocity: Vec3) {
        entity.push(velocity)

        val target = entity.target
        if (target != null) {
            entity.lookControl.setLookAt(target.position())
            entity.lookAtTarget(target.position(), entity.headRotSpeed.toFloat(), entity.maxHeadXRot.toFloat())
        }
    }

    fun buildWanderGoal(): VelocityGoal {
        val tooFarFromTarget: (Vec3) -> Boolean = getWithinDistancePredicate(idleWanderDistance) { entity.idlePosition }
        val movingTowardsIdleCenter: (Vec3) -> Boolean = { MathUtils.movingTowards(entity.idlePosition, entity.position(), it) }
        val canMoveTowardsPositionValidator = ValidDirectionAnd(
            listOf(
                CanMoveThrough(entity, reactionDistance),
                InDesiredRange({ false }, tooFarFromTarget, movingTowardsIdleCenter)
            )
        )
        val targetSelector = ValidatedTargetSelector(
            iEntity,
            canMoveTowardsPositionValidator,
            ModRandom()
        )
        return VelocityGoal(
            ::moveTowards,
            createSteering(),
            targetSelector
        )
    }

    private fun createSteering() =
        VelocitySteering(iEntity, entity.getAttributeValue(Attributes.FLYING_SPEED), 120.0)

    private fun getWithinDistancePredicate(distance: Double, targetPos: () -> Vec3): (Vec3) -> Boolean = {
        val target = entity.position().add(it.scale(reactionDistance))
        MathUtils.withinDistance(target, targetPos(), distance)
    }

    private fun moveTowards(velocity: Vec3) {
        entity.push(velocity)

        val lookTarget = entity.position().add(newVec3d(y = entity.eyeHeight.toDouble())).add(velocity)
        entity.lookControl.setLookAt(lookTarget)
        entity.lookAtTarget(lookTarget, entity.headRotSpeed.toFloat(), entity.maxHeadXRot.toFloat())
    }
}