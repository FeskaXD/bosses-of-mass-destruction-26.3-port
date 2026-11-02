package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.shieldPiercing
import net.barribob.boss.utils.NetworkUtils.Companion.sendVelocity
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.Mob
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3

class WaveAction(val entity: Mob) :
    IActionWithCooldown {
    private val riftRadius = 4.0
    private val circlePoints = MathUtils.buildBlockCircle(riftRadius)
    private val world = entity.level()
    private val eventScheduler = ModComponents.getWorldEventScheduler(world)

    override fun perform(): Int {
        val target = entity.target
        if(target !is LivingEntity) return 80
        placeRifts(target)
        return 80
    }

    private fun placeRifts(target: LivingEntity) {
        val riftBurst = RiftBurst(
            entity,
            world as ServerLevel,
            Particles.OBSIDILITH_WAVE_INDICATOR,
            Particles.OBSIDILITH_WAVE,
            waveDelay,
            eventScheduler,
            ::damageEntity
        )

        world.playSound(entity.position(), Mod.sounds.obsidilithPrepareAttack, SoundSource.HOSTILE, 3.0f, 0.8f, 64.0)
        eventScheduler.addEvent(TimedEvent({
            val direction = MathUtils.unNormedDirection(entity.position(), target.position()).normalize().scale(riftRadius)
            val numRifts = 5
            val startRiftPos = entity.position().add(direction)
            val endRiftPos = startRiftPos.add(direction.scale(numRifts.toDouble() * 1.5))
            MathUtils.lineCallback(startRiftPos, endRiftPos, numRifts) { linePos, i ->
                eventScheduler.addEvent(TimedEvent({
                    world.playSound(linePos, Mod.sounds.waveIndicator, SoundSource.HOSTILE, 0.7f, range = 32.0)
                    eventScheduler.addEvent(TimedEvent({
                        world.playSound(linePos, Mod.sounds.obsidilithWave, SoundSource.HOSTILE, 1.2f, range = 32.0)
                    }, waveDelay, shouldCancel = { !entity.isAlive }))

                    for (point in circlePoints) {
                        riftBurst.tryPlaceRift(linePos.add(point))
                    }
                }, i * 8, shouldCancel = { !entity.isAlive }))
            }
        }, attackStartDelay, shouldCancel = { !entity.isAlive }))
    }

    private fun damageEntity(entity: LivingEntity) {
        val damage = this.entity.getAttributeValue(Attributes.ATTACK_DAMAGE).toFloat()
        entity.sendVelocity(Vec3(entity.deltaMovement.x, 0.8, entity.deltaMovement.z))
        entity.igniteForSeconds(5.0f)
        entity.hurt(entity.level().damageSources().shieldPiercing(entity.level(), this.entity), damage)
    }

    companion object {
        const val waveDelay = 20
        const val attackStartDelay = 20
    }
}