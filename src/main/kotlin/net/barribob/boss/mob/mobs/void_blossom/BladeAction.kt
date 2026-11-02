package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.Mod
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.void_blossom.hitbox.HitboxId
import net.barribob.boss.mob.utils.ProjectileData
import net.barribob.boss.mob.utils.ProjectileThrower
import net.barribob.boss.projectile.PetalBladeProjectile
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.EventSeries
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.rotateVector
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource

class BladeAction(private val entity: VoidBlossomEntity, private val eventScheduler: EventScheduler, private val shouldCancel: () -> Boolean) : IActionWithCooldown {
    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayer) return 80

        eventScheduler.addEvent(
            EventSeries(
                TimedEvent({
                    entity.entityData.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.Petal.id)
                }, 10, shouldCancel = shouldCancel),
                TimedEvent({
                    entity.entityData.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.Idle.id)
                }, 90)
            )
        )

        val thrower = {
            val eyePos = target.boundingBox.center
            val dir = entity.eyePosition.subtract(eyePos)
            val left = dir.cross(VecUtils.yAxis).normalize()
            val rotation = RandomUtils.randSign() * 20.0
            val angled = left.rotateVector(dir, rotation)
            val lineStart = eyePos.add(angled.scale(7.0))
            val lineEnd = eyePos.add(angled.scale(-7.0))
            val projectileThrower = ProjectileThrower {
                val projectile = PetalBladeProjectile(entity, entity.level(), {}, listOf(entity.type), rotation.toFloat())
                projectile.setPos(entity.eyePosition.add(VecUtils.yAxis))
                projectile.setNoGravity(true)
                ProjectileData(projectile, 0.9f, 0f, 0.0)
            }

            target.level().playSound(entity.position(), Mod.sounds.petalBlade, SoundSource.HOSTILE, 3.0f, target.random.randomPitch(), 64.0)

            MathUtils.lineCallback(lineStart, lineEnd, 11) { vec3d, _ ->
                projectileThrower.throwProjectile(vec3d)
            }
        }

        eventScheduler.addEvent(TimedEvent(thrower, 28, shouldCancel = shouldCancel))
        eventScheduler.addEvent(TimedEvent(thrower, 52, shouldCancel = shouldCancel))
        eventScheduler.addEvent(TimedEvent(thrower, 75, shouldCancel = shouldCancel))

        return 120
    }
}