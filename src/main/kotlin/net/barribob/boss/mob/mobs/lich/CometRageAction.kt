package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.config.LichConfig
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.utils.ProjectileData
import net.barribob.boss.mob.utils.ProjectileThrower
import net.barribob.boss.projectile.comet.CometProjectile
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.barribob.maelstrom.static_utilities.setPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class CometRageAction(
    private val entity: LichEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean,
    private val lichConfig: LichConfig
) : IActionWithCooldown {
    private val cometThrower = { offset: Vec3 ->
        ProjectileThrower {
            val projectile = CometProjectile(entity, entity.level(), {
                entity.level().explode(
                    entity,
                    it.x,
                    it.y,
                    it.z,
                    lichConfig.comet.explosionStrength,
                    Level.ExplosionInteraction.MOB
                )
            }, listOf(MinionAction.summonEntityType))
            projectile.setPos(entity.eyePos().add(offset))
            ProjectileData(projectile, 1.6f, 0f)
        }
    }

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayer) return rageCometsMoveDuration
        performCometThrow(target)
        return rageCometsMoveDuration
    }

    private fun performCometThrow(target: ServerPlayer) {
        for ((i, offset) in getRageCometOffsets(entity).withIndex()) {
            eventScheduler.addEvent(TimedEvent({
                val targetPos = target.boundingBox.center
                cometThrower(offset).throwProjectile(targetPos)
                target.level().playSound(entity.position(), Mod.sounds.cometShoot, SoundSource.HOSTILE, 3.0f, range = 64.0)
            }, initialRageCometDelay + (i * delayBetweenRageComets), shouldCancel = shouldCancel))
        }
        target.level().playSound(entity.position(), Mod.sounds.ragePrepare, SoundSource.HOSTILE, 1.0f, range = 64.0)
    }

    companion object {
        private const val numCometsDuringRage = 6
        const val initialRageCometDelay = 60
        const val delayBetweenRageComets = 30
        const val rageCometsMoveDuration = initialRageCometDelay + (numCometsDuringRage * delayBetweenRageComets)

        fun getRageCometOffsets(entity: LichEntity): List<Vec3> {
            val offsets = mutableListOf<Vec3>()
            MathUtils.circleCallback(3.0, numCometsDuringRage, entity.lookAngle) { offsets.add(it) }
            return offsets
        }
    }
}