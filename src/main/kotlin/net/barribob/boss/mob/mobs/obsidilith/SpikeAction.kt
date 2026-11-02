package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.shieldPiercing
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Mob
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource

class SpikeAction(val entity: Mob) : IActionWithCooldown {
    private val eventScheduler = ModComponents.getWorldEventScheduler(entity.level())
    private val circlePoints = MathUtils.buildBlockCircle(2.0)

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayer) return 80
        placeSpikes(target)
        return 100
    }

    private fun placeSpikes(target: ServerPlayer) {
        val riftTime = 20
        val riftBurst = RiftBurst(
            entity,
            target.level(),
            Particles.OBSIDILITH_SPIKE_INDICATOR,
            Particles.OBSIDILITH_SPIKE,
            riftTime,
            eventScheduler,
            ::damageEntity
        )

        target.level().playSound(entity.position(), Mod.sounds.obsidilithPrepareAttack, SoundSource.HOSTILE, 3.0f, 1.2f, 64.0)

        for (i in 0 until 3) {
            val timeBetweenRifts = 30
            val initialDelay = 30
            eventScheduler.addEvent(TimedEvent({
                val placement =
                    ObsidilithUtils.approximatePlayerNextPosition(ModComponents.getPlayerPositions(target), target.position())
                target.level().playSound(placement, Mod.sounds.spikeIndicator, SoundSource.HOSTILE, 1.0f, range = 32.0)

                eventScheduler.addEvent(TimedEvent({
                    target.level().playSound(
                        placement,
                        Mod.sounds.spike,
                        SoundSource.HOSTILE,
                        1.2f,
                        range = 32.0
                    )
                }, riftTime, shouldCancel = { !entity.isAlive }))

                for (point in circlePoints) {
                    riftBurst.tryPlaceRift(placement.add(point))
                }
            }, initialDelay + i * timeBetweenRifts, shouldCancel = { !entity.isAlive }))
        }
    }

    private fun damageEntity(entity: LivingEntity) {
        val damage = this.entity.getAttributeValue(Attributes.ATTACK_DAMAGE).toFloat()
        entity.hurt(entity.level().damageSources().shieldPiercing(entity.level(), this.entity), damage)
        entity.addEffect(MobEffectInstance(MobEffects.SLOWNESS, 120, 2))
    }
}