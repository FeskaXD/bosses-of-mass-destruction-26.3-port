package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.config.LichConfig
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.utils.ProjectileData
import net.barribob.boss.mob.utils.ProjectileThrower
import net.barribob.boss.projectile.MagicMissileProjectile
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.world.entity.Entity
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3

class VolleyAction(
    private val entity: LichEntity,
    mobConfig: LichConfig,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) : IActionWithCooldown {
    private val missileStatusDuration = mobConfig.missile.statusEffectDuration
    private val missileStatusPotency = mobConfig.missile.statusEffectPotency

    private val missileThrower = { offset: Vec3 ->
        ProjectileThrower {
            val projectile = MagicMissileProjectile(entity, entity.level(), {
                it.addEffect(
                    MobEffectInstance(MobEffects.SLOWNESS,
                        missileStatusDuration,
                        missileStatusPotency)
                )
            }, listOf(MinionAction.summonEntityType))
            projectile.setPos(entity.eyePos().add(offset))
            ProjectileData(projectile, 1.6f, 0f)
        }
    }

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayer) return missileThrowCooldown
        performVolley(target)
        return missileThrowCooldown
    }

    private fun performVolley(target: ServerPlayer) {
        eventScheduler.addEvent(
            TimedEvent(
                {
                    val targetPos = target.boundingBox.center
                    for (offset in getMissileLaunchOffsets(entity)) {
                        missileThrower(offset).throwProjectile(targetPos.add(offset.planeProject(VecUtils.yAxis)))
                    }
                    target.level().playSound(
                        entity.position(),
                        Mod.sounds.missileShoot,
                        SoundSource.HOSTILE,
                        3.0f,
                        range = 64.0
                    )
                },
                missileThrowDelay, shouldCancel = shouldCancel
            )
        )
        eventScheduler.addEvent(
            TimedEvent({
                target.level().playSound(
                    entity.position(),
                    Mod.sounds.missilePrepare,
                    SoundSource.HOSTILE,
                    4.0f,
                    range = 64.0
                )
            }, 10, shouldCancel = shouldCancel)
        )
    }

    companion object {
        const val missileThrowDelay = 46
        const val missileThrowCooldown = 80
        const val missileParticleSummonDelay = 16

        fun getMissileLaunchOffsets(entity: Entity): List<Vec3> = listOf(
            MathUtils.axisOffset(entity.lookAngle, VecUtils.yAxis.add(VecUtils.zAxis.scale(2.0))),
            MathUtils.axisOffset(entity.lookAngle, VecUtils.yAxis.scale(1.5).add(VecUtils.zAxis)),
            MathUtils.axisOffset(entity.lookAngle, VecUtils.yAxis.scale(2.0)),
            MathUtils.axisOffset(entity.lookAngle, VecUtils.yAxis.scale(1.5).add(VecUtils.zAxis.negateServer())),
            MathUtils.axisOffset(entity.lookAngle, VecUtils.yAxis.add(VecUtils.zAxis.negateServer().scale(2.0)))
        )
    }
}