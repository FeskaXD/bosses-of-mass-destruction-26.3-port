package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.findGroundBelow
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.Event
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class AnvilAction(private val actor: Mob, val explosionPower: Float) : IActionWithCooldown {
    private val eventScheduler = ModComponents.getWorldEventScheduler(actor.level())
    private val circlePoints = MathUtils.buildBlockCircle(2.0)

    override fun perform(): Int {
        val target = actor.target
        val serverWorld = actor.level()
        if (target !is LivingEntity || serverWorld !is ServerLevel) return 80
        performAttack(target, serverWorld)
        return 80
    }

    private fun performAttack(target: LivingEntity, serverWorld: ServerLevel) {
        serverWorld.playSound(actor.position(), Mod.sounds.obsidilithPrepareAttack, SoundSource.HOSTILE, 3.0f, 1.0f, 64.0)

        eventScheduler.addEvent(TimedEvent({
            val targetPos = target.position()
            val teleportPos = targetPos.add(VecUtils.yAxis.scale(24.0))
            val originalPos = actor.position()
            actor.snapTo(teleportPos.x, teleportPos.y, teleportPos.z, actor.yRot, actor.xRot)
            serverWorld.playSound(teleportPos, Mod.sounds.obsidilithTeleport, SoundSource.HOSTILE, 3.0f, range = 64.0)

            for(pos in circlePoints) {
                val particlePos = actor.level().findGroundBelow(BlockPos.containing(pos.add(targetPos)).above(3)).above()
                if(particlePos.y != 0) {
                    serverWorld.spawnParticle(Particles.OBSIDILITH_ANVIL_INDICATOR, particlePos.asVec3d().add(Vec3(0.5, 0.1, 0.5)), Vec3.ZERO)
                }
            }

            val shouldLand = { actor.onGround() || actor.y < 0 }
            val shouldCancelLand = { !actor.isAlive || shouldLand() }
            eventScheduler.addEvent(Event(shouldLand, {
                actor.level().explode(
                    actor,
                    actor.x,
                    actor.y,
                    actor.z,
                    explosionPower,
                    Level.ExplosionInteraction.MOB
                )
                eventScheduler.addEvent(TimedEvent({
                    actor.snapTo(originalPos.x, originalPos.y, originalPos.z, actor.yRot, actor.xRot)
                    serverWorld.playSound(actor.position(), Mod.sounds.obsidilithTeleport, SoundSource.HOSTILE, 1.0f, range = 64.0)
                }, 20, shouldCancel = { !actor.isAlive}))
            }, shouldCancel = shouldCancelLand))

        }, 20, shouldCancel = { !actor.isAlive }))
    }
}