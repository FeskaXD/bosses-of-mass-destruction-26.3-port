package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.ai.ValidatedTargetSelector
import net.barribob.boss.mob.ai.VelocitySteering
import net.barribob.boss.mob.ai.goals.VelocityGoal
import net.barribob.boss.mob.ai.valid_direction.CanMoveThrough
import net.barribob.boss.mob.ai.valid_direction.InDesiredRange
import net.barribob.boss.mob.ai.valid_direction.ValidDirectionAnd
import net.barribob.boss.mob.utils.EntityAdapter
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.phys.Vec3

class GauntletMovement(val entity: GauntletEntity) {
    private val reactionDistance = 4.0
    private val iEntity = EntityAdapter(entity)
    private val tooFarFromTargetDistance = 25.0
    private val tooCloseToTargetDistance = 5.0

    fun buildAttackMovement(): VelocityGoal {
        val targetPos = { entity.safeGetTargetPos()}
        val tooCloseToTarget: (Vec3) -> Boolean = getWithinDistancePredicate(tooCloseToTargetDistance, targetPos)
        val tooFarFromTarget: (Vec3) -> Boolean = { !getWithinDistancePredicate(tooFarFromTargetDistance, targetPos)(it) }
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
            ::moveAndLookAtTarget,
            VelocitySteering(iEntity, entity.getAttributeValue(Attributes.FLYING_SPEED), 120.0),
            targetSelector
        )
    }

    private fun moveAndLookAtTarget(velocity: Vec3) {
        entity.push(velocity)

        val target = entity.target
        if (target != null) {
            entity.lookControl.setLookAt(target.eyePos())
            entity.lookAt(target, entity.headRotSpeed.toFloat(), entity.maxHeadXRot.toFloat())
        }
    }

    private fun getWithinDistancePredicate(distance: Double, targetPos: () -> Vec3): (Vec3) -> Boolean = {
        val target = entity.position().add(it.scale(reactionDistance))
        MathUtils.withinDistance(target, targetPos(), distance)
    }
}