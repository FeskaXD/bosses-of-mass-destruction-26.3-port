package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.gauntlet.GauntletEntity.Companion.laserTarget
import net.barribob.boss.utils.ModUtils.findEntitiesInLine
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.VanillaCopies.destroyBlocks
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.EventSeries
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.ClipContext

class LaserAction(
    val entity: GauntletEntity,
    val eventScheduler: EventScheduler,
    private val cancelAction: () -> Boolean,
    private val serverWorld: ServerLevel
) : IActionWithCooldown {
    override fun perform(): Int {
        val target = entity.target ?: return 40

        val laserRenderPositions = HistoricalData<Vec3>(Vec3.ZERO, laserLagTicks)

        serverWorld.playSound(entity.position(), Mod.sounds.gauntletLaserCharge, SoundSource.HOSTILE, 3.0f, 1.0f, 64.0)

        val sendStartToClient = TimedEvent({
            entity.entityData.set(laserTarget, target.id)
        }, 25, shouldCancel = cancelAction)

        val applyLaser = TimedEvent({
            laserRenderPositions.set(target.boundingBox.center)
            if (laserRenderPositions.getSize() == laserRenderPositions.maxHistory) {
                applyLaser(laserRenderPositions)
            }
        }, 0, 60, cancelAction)

        val stop = TimedEvent({
            laserRenderPositions.clear()
            entity.entityData.set(laserTarget, 0)
            entity.level().broadcastEntityEvent(entity, GauntletAttacks.laserAttackStop)
        }, 0)

        eventScheduler.addEvent(EventSeries(sendStartToClient, applyLaser, stop))

        return 120
    }

    private fun applyLaser(laserRenderPositions: HistoricalData<Vec3>) {
        val targetLaserPos = laserRenderPositions.getAll().first()
        val extendedLaserPos = extendLaser(entity, targetLaserPos)
        val result = entity.level().clip(
            ClipContext(
                entity.eyePos(),
                extendedLaserPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
            )
        )

        if (result.type == HitResult.Type.BLOCK) {
            if (entity.tickCount % 2 == 0) {
                entity.destroyBlocks(AABB(result.location, result.location).inflate(0.1))
            }
            applyLaserToEntities(result.location)
        } else {
            applyLaserToEntities(extendedLaserPos)
        }
    }

    private fun applyLaserToEntities(laserTargetPos: Vec3) {
        val entitiesHit = entity.level().findEntitiesInLine(entity.eyePos(), laserTargetPos, entity)
            .filterIsInstance<LivingEntity>()
        for (hitEntity in entitiesHit) {
            val originalAttack = entity.getAttributeValue(Attributes.ATTACK_DAMAGE)
            entity.getAttribute(Attributes.ATTACK_DAMAGE)?.baseValue = originalAttack * 0.75
            entity.doHurtTarget(entity.level() as ServerLevel, hitEntity)
            entity.getAttribute(Attributes.ATTACK_DAMAGE)?.baseValue = originalAttack
        }
    }

    companion object {
        const val laserLagTicks = 8
        fun extendLaser(entity: Entity, laserTargetPos: Vec3): Vec3 =
            MathUtils.unNormedDirection(entity.eyePos(), laserTargetPos).normalize().scale(30.0).add(entity.eyePos())
    }
}