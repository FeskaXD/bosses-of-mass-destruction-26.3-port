package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithUtils
import net.barribob.boss.mob.mobs.void_blossom.hitbox.HitboxId
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.NetworkUtils.Companion.sendSpikePacket
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.EventSeries
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource

class SpikeAction(
    val entity: VoidBlossomEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) : IActionWithCooldown {
    private val circlePoints = MathUtils.buildBlockCircle(2.0)

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayer) return 80
        placeSpikes(target)
        return 150
    }

    private fun placeSpikes(target: ServerPlayer) {
        val riftBurst = Spikes(
            entity,
            target.level(),
            Particles.VOID_BLOSSOM_SPIKE_INDICATOR,
            indicatorDelay,
            eventScheduler,
            shouldCancel
        )

        target.level().playSound(
            entity.position(),
            Mod.sounds.voidBlossomBurrow,
            SoundSource.HOSTILE,
            1.5f,
            range = 32.0
        )

        eventScheduler.addEvent(
            EventSeries(
                TimedEvent({
                    entity.entityData.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.Spike.id)
                }, 20, shouldCancel = shouldCancel),
                TimedEvent({
                    entity.entityData.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.Idle.id)
                }, 100)
            )
        )

        for (i in 0 until 3) {
            val timeBetweenRifts = 30
            val initialDelay = 30
            eventScheduler.addEvent(TimedEvent({
                val placement =
                    ObsidilithUtils.approximatePlayerNextPosition(ModComponents.getPlayerPositions(target), target.position())
                target.level().playSound(
                    placement,
                    Mod.sounds.voidSpikeIndicator,
                    SoundSource.HOSTILE,
                    1.0f,
                    range = 32.0
                )

                val successfulSpikes = circlePoints.flatMap { riftBurst.tryPlaceRift(placement.add(it)) }

                eventScheduler.addEvent(TimedEvent({
                    target.level().playSound(
                        placement,
                        Mod.sounds.voidBlossomSpike,
                        SoundSource.HOSTILE,
                        1.2f,
                        range = 32.0
                    )
                    entity.sendSpikePacket(successfulSpikes)
                }, indicatorDelay, shouldCancel = shouldCancel))
            }, initialDelay + i * timeBetweenRifts, shouldCancel = shouldCancel))
        }
    }

    companion object {
        const val indicatorDelay = 20
    }
}