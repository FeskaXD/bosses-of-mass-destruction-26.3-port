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
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3

class VolleyRageAction(
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
        if (target !is ServerPlayer) return 80
        return performVolley(target)
    }

    private fun performVolley(target: ServerPlayer) : Int {
        val rageMissileVolleys = getRageMissileVolleys(entity).size
        target.level().playSound(entity.position(), Mod.sounds.missilePrepare, SoundSource.HOSTILE, 4.0f, range = 64.0)
        for (i in 0 until rageMissileVolleys) {
            eventScheduler.addEvent(TimedEvent({
                val targetPos = target.boundingBox.center
                for (offset in getRageMissileVolleys(entity)[i]) {
                    missileThrower(offset).throwProjectile(targetPos.add(offset))
                }
                target.level().playSound(entity.position(), Mod.sounds.missileShoot, SoundSource.HOSTILE, 3.0f, range = 64.0)
            },
                ragedMissileVolleyInitialDelay + (i * ragedMissileVolleyBetweenVolleyDelay),
                shouldCancel = shouldCancel))
        }
        return ragedMissileVolleyInitialDelay + (rageMissileVolleys * ragedMissileVolleyBetweenVolleyDelay)
    }

    companion object {
        const val ragedMissileVolleyInitialDelay = 60
        const val ragedMissileVolleyBetweenVolleyDelay = 30
        const val ragedMissileParticleDelay = 30

        fun getRageMissileVolleys(entity: LichEntity): List<List<Vec3>> {
            val xOffset = 3.0
            val zOffset = 4.0
            val numPoints = 9
            val lineStart = MathUtils.axisOffset(entity.lookAngle, VecUtils.xAxis
                .scale(xOffset)
                .add(VecUtils.zAxis.scale(zOffset)))
            val lineEnd = MathUtils.axisOffset(entity.lookAngle, VecUtils.xAxis
                .scale(xOffset)
                .add(VecUtils.zAxis.scale(-zOffset)))
            val lineAcross = mutableListOf<Vec3>()
            MathUtils.lineCallback(lineStart, lineEnd, numPoints) { v, _ -> lineAcross.add(v) }
            val lineUpDown = lineAcross.map { it.rotateVector(entity.lookAngle, 90.0) }
            val cross = lineAcross + lineUpDown
            val xVolley = cross.map { it.rotateVector(entity.lookAngle, 45.0) }

            return listOf(lineAcross, lineUpDown, cross, xVolley)
        }
    }
}