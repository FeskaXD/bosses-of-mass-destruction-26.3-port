package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.VanillaCopies.destroyBlocks
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class PunchAction(
    val entity: GauntletEntity,
    val eventScheduler: EventScheduler,
    private val mobConfig: GauntletConfig,
    private val cancelAction: () -> Boolean,
    private val serverWorld: ServerLevel
) : IActionWithCooldown {
    private var previousSpeed = 0.0

    override fun perform(): Int {
        val target = entity.target ?: return 40
        val targetPos = entity.eyePos().add(MathUtils.unNormedDirection(entity.eyePos(), target.position()).scale(1.2))
        val accelerateStartTime = 16
        val unclenchTime = 56

        val breakBoundCenter = BlockPos.containing(entity.position().add(entity.lookAngle))
        val breakBounds = AABB.encapsulatingFullBlocks(breakBoundCenter.subtract(BlockPos(1, 1, 1)), breakBoundCenter.offset(1, 2, 1))
        entity.destroyBlocks(breakBounds)
        entity.push(0.0, 0.7, 0.0)
        eventScheduler.addEvent(TimedEvent({
            serverWorld.playSound(
                entity.position(),
                Mod.sounds.gauntletClink,
                SoundSource.HOSTILE,
                2.0f,
                pitch = entity.random.randomPitch() * 0.8f
            )
        }, 12, shouldCancel = cancelAction))

        var velocityStack = 0.6
        eventScheduler.addEvent(
            TimedEvent(
                {
                    entity.accelerateTowardsTarget(targetPos, velocityStack)
                    velocityStack = 0.32
                },
                accelerateStartTime,
                15,
                { entity.position().distanceToSqr(targetPos) < 9 || cancelAction() })
        )
        eventScheduler.addEvent(TimedEvent(::whilePunchActive, accelerateStartTime, unclenchTime - accelerateStartTime, cancelAction))

        val closeFistAnimationTime = 7
        eventScheduler.addEvent(TimedEvent(entity.hitboxHelper::setClosedFistHitbox, closeFistAnimationTime, shouldCancel = cancelAction))

        eventScheduler.addEvent(TimedEvent({
            entity.level().broadcastEntityEvent(
                entity,
                GauntletAttacks.stopPunchAnimation
            )
        }, unclenchTime, shouldCancel = cancelAction))
        eventScheduler.addEvent(TimedEvent(entity.hitboxHelper::setOpenHandHitbox, unclenchTime + 8))

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

    private fun testEntityImpact() {
        val collidedEntities =
            entity.level().getEntitiesOfClass(LivingEntity::class.java, entity.boundingBox) { it != entity }
        for (target in collidedEntities) {
            entity.doHurtTarget(entity.level() as ServerLevel, target)
            target.push(entity.deltaMovement.scale(0.5))
        }
    }

    companion object {
        fun Entity.accelerateTowardsTarget(target: Vec3, velocity: Double) {
            val dir: Vec3 = MathUtils.unNormedDirection(this.eyePos(), target).normalize()
            val velocityCorrection: Vec3 = this.deltaMovement.planeProject(dir)
            this.push(dir.subtract(velocityCorrection).scale(velocity))
        }
    }
}