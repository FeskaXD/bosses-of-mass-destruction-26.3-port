package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.mobs.gauntlet.GauntletEntity.Companion.laserTarget
import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.boss.mob.utils.IStatusHandler
import net.barribob.boss.mob.utils.ITrackedDataHandler
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.world.entity.monster.Guardian
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.ClipContext

/**
 * Rendering based on [Guardian]
 */
class GauntletClientLaserHandler(val entity: GauntletEntity, val eventScheduler: EventScheduler) :
    IEntityTick<ClientLevel>, ITrackedDataHandler, IStatusHandler {
    private var cachedBeamTarget: LivingEntity? = null
    private val laserRenderPositions =
        HistoricalData<Pair<Vec3, Vec3>>(Pair(Vec3.ZERO, Vec3.ZERO), LaserAction.laserLagTicks)
    val laserChargeParticles = ClientParticleBuilder(Particles.SPARKLES)
        .brightness(Particles.FULL_BRIGHT)
        .color(ModColors.LASER_RED)
        .colorVariation(0.2)

    override fun tick(world: ClientLevel) {
        val beamTarget = getBeamTarget()
        if (beamTarget != null) {
            val centerBoxOffset = beamTarget.boundingBox.center.subtract(beamTarget.position())
            laserRenderPositions.set(
                Pair(
                    beamTarget.position().add(centerBoxOffset),
                    beamTarget.lastRenderPos.add(centerBoxOffset)
                )
            )
        } else {
            laserRenderPositions.clear()
        }
    }

    @Environment(EnvType.CLIENT)
    fun shouldRenderLaser() = laserRenderPositions.getSize() > 1

    @Environment(EnvType.CLIENT)
    fun getLaserRenderPos(): Pair<Vec3, Vec3> {
        val laserPos = laserRenderPositions.getAll().first()
        val newPos = LaserAction.extendLaser(entity, laserPos.first)
        val prevPos = LaserAction.extendLaser(entity, laserPos.second)
        val newResult = entity.level().clip(
            ClipContext(
                entity.eyePos(),
                newPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
            )
        )
        val prevResult = entity.level().clip(
            ClipContext(
                entity.eyePos(),
                prevPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
            )
        )
        val colNewPos = if (newResult.type == HitResult.Type.BLOCK) newResult.location else newPos
        val colPrevPos = if (prevResult.type == HitResult.Type.BLOCK) prevResult.location else prevPos
        return Pair(colNewPos, colPrevPos)
    }

    private fun hasBeamTarget(): Boolean = entity.entityData.get(laserTarget) as Int != 0

    private fun getBeamTarget(): LivingEntity? {
        return if (!hasBeamTarget()) {
            null
        } else if (entity.level().isClientSide) {
            if (this.cachedBeamTarget != null) {
                this.cachedBeamTarget
            } else {
                val entity = entity.level().getEntity((entity.entityData.get(laserTarget) as Int))
                if (entity is LivingEntity) {
                    this.cachedBeamTarget = entity
                    this.cachedBeamTarget
                } else {
                    null
                }
            }
        } else {
            entity.target
        }
    }

    override fun onTrackedDataSet(data: EntityDataAccessor<*>) {
        if (laserTarget == data) {
            this.cachedBeamTarget = null
        }
    }

    override fun handleClientStatus(status: Byte) {
        if (status == GauntletAttacks.laserAttack) {
            eventScheduler.addEvent(TimedEvent({
                val lookVec = entity.lookAngle
                for (i in 0..1) {
                    val circularOffset =
                        lookVec.cross(VecUtils.yAxis).rotateVector(lookVec, RandomUtils.range(0, 359).toDouble())
                    val velocity = circularOffset.normalize().reverse().scale(0.07).add(entity.deltaMovement.scale(1.2))
                    val position = entity.eyePos().add(circularOffset).add(lookVec.scale(0.5))
                    laserChargeParticles.build(position, velocity)
                }
            }, 0, 85))
        }
    }
}