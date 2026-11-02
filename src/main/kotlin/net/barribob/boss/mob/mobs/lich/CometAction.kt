package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.config.LichConfig
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.ai.action.ThrowProjectileAction
import net.barribob.boss.mob.utils.ProjectileData
import net.barribob.boss.mob.utils.ProjectileThrower
import net.barribob.boss.projectile.comet.CometProjectile
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.barribob.maelstrom.static_utilities.setPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class CometAction(
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
        if (target !is ServerPlayer) return cometThrowCooldown
        performCometThrow(target.level())
        return cometThrowCooldown
    }

    private fun performCometThrow(serverWorld: ServerLevel) {
        eventScheduler.addEvent(
            TimedEvent(
                {
                    serverWorld.playSound(
                        entity.position(),
                        Mod.sounds.cometPrepare,
                        SoundSource.HOSTILE,
                        3.0f,
                        range = 64.0
                    )
                }, 10, shouldCancel = shouldCancel
            )
        )
        eventScheduler.addEvent(
            TimedEvent(
                {
                    ThrowProjectileAction(entity, cometThrower(getCometLaunchOffset())).perform()
                    serverWorld.playSound(entity.position(), Mod.sounds.cometShoot, SoundSource.HOSTILE, 3.0f, range = 64.0)
                },
                cometThrowDelay,
                shouldCancel = shouldCancel
            )
        )
    }

    companion object {
        const val cometThrowDelay = 60
        const val cometParticleSummonDelay = 15
        const val cometThrowCooldown = 80
        fun getCometLaunchOffset(): Vec3 = VecUtils.yAxis.scale(2.0)
    }
}