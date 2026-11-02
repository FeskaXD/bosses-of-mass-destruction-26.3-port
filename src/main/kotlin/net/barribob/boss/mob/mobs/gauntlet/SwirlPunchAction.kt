package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.gauntlet.GauntletEntity.Companion.isEnergized
import net.barribob.boss.mob.mobs.gauntlet.PunchAction.Companion.accelerateTowardsTarget
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class SwirlPunchAction(
    val entity: GauntletEntity,
    val eventScheduler: EventScheduler,
    private val mobConfig: GauntletConfig,
    private val cancelAction: () -> Boolean,
    private val serverWorld: ServerLevel
) : IActionWithCooldown {
    private var previousSpeed = 0.0

    override fun perform(): Int {
        val target = entity.target ?: return 40
        val targetDirection = MathUtils.unNormedDirection(entity.eyePos(), target.boundingBox.center)
        val targetPos = entity.eyePos().add(targetDirection.scale(1.2))
        val accelerateStartTime = 30
        val unclenchTime = 60
        val closeFistAnimationTime = 7

        entity.push(0.0, 0.7, 0.0)
        serverWorld.playSound(
            entity.position(),
            Mod.sounds.gauntletSpinPunch,
            SoundSource.HOSTILE,
            2.0f,
            1.0f,
            64.0
        )
        entity.entityData.set(isEnergized, true)
        eventScheduler.addEvent(TimedEvent(entity.hitboxHelper::setClosedFistHitbox, closeFistAnimationTime, shouldCancel = cancelAction))

        var velocityStack = 0.6
        eventScheduler.addEvent(
            TimedEvent(
                {
                    entity.accelerateTowardsTarget(targetPos, velocityStack)
                    velocityStack = 0.40
                },
                accelerateStartTime,
                15,
                { entity.position().distanceToSqr(targetPos) < 9 || cancelAction() })
        )
        eventScheduler.addEvent(TimedEvent(::whilePunchActive, accelerateStartTime, unclenchTime - accelerateStartTime, cancelAction))
        eventScheduler.addEvent(TimedEvent({
            entity.hitboxHelper.setOpenHandHitbox()
            entity.entityData.set(isEnergized, false)
        }, unclenchTime))

        return 80
    }

    private fun whilePunchActive() {
        testBlockPhysicalImpact()
        testEntityImpact()
        val speed = entity.deltaMovement.length()
        previousSpeed = speed
    }

    private fun testBlockPhysicalImpact() {
        if ((entity.horizontalCollision || entity.verticalCollision) && previousSpeed > 0.55f) {
            val pos: Vec3 = entity.position()
            if (entity.entityData.get(isEnergized)) {
                entity.level().explode(
                    entity,
                    pos.x,
                    pos.y,
                    pos.z,
                    mobConfig.energizedPunchExplosionSize.toFloat(),
                    true,
                    Level.ExplosionInteraction.MOB
                )
                entity.entityData.set(isEnergized, false)
            } else {
                entity.level().explode(
                    entity,
                    pos.x,
                    pos.y,
                    pos.z,
                    (previousSpeed * mobConfig.normalPunchExplosionMultiplier).toFloat(),
                    Level.ExplosionInteraction.MOB
                )
            }
        }
    }

    private fun testEntityImpact() {
        val collidedEntities = entity.level().getEntitiesOfClass(LivingEntity::class.java, entity.boundingBox) { it != entity }
        for (target in collidedEntities) {
            entity.doHurtTarget(entity.level() as ServerLevel, target)
            target.push(entity.deltaMovement.scale(0.5))
        }
    }
}