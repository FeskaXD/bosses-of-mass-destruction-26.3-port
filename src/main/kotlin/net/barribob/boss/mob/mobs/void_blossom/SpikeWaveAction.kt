package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.Mod
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.void_blossom.hitbox.HitboxId
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.NetworkUtils.Companion.sendSpikePacket
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.EventSeries
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvent
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

class SpikeWaveAction(
    val entity: VoidBlossomEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) : IActionWithCooldown {
    private val firstCirclePoints = MathUtils.buildBlockCircle(7.0)
    private val secondRadius = 14.0
    private val secondCirclePoints = MathUtils.buildBlockCircle(secondRadius).subtract(firstCirclePoints).toList()
    private val thirdRadius = 21.0
    private val thirdCirclePoints =
        MathUtils.buildBlockCircle(thirdRadius).subtract(secondCirclePoints).subtract(firstCirclePoints).toList()

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayer) return 80
        placeRifts(target)
        return 120
    }

    private fun placeRifts(target: ServerPlayer) {
        val firstBurstDelay = 20
        val secondBurstDelay = 45
        val thirdBurstDelay = 70
        val world = target.level()

        eventScheduler.addEvent(
            EventSeries(
                TimedEvent({
                    entity.entityData.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.SpikeWave1.id)
                }, 20, shouldCancel = shouldCancel),
                TimedEvent({
                    entity.entityData.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.SpikeWave2.id)
                }, 26, shouldCancel = shouldCancel),
                TimedEvent({
                    entity.entityData.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.SpikeWave3.id)
                }, 26, shouldCancel = shouldCancel),
                TimedEvent({
                    entity.entityData.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.Idle.id)
                }, 26)
            )
        )

        val spikeGenerator = Spikes(
            entity,
            world,
            Particles.VOID_BLOSSOM_SPIKE_WAVE_INDICATOR,
            indicatorDelay,
            eventScheduler,
            shouldCancel
        )

        createSpikeWave(
            {
                createBurst(spikeGenerator, firstCirclePoints)
                world.playSound(entity.position(), Mod.sounds.spikeWaveIndicator, SoundSource.HOSTILE, 2.0f, 0.7f, 64.0)
            },
            { world.playSound(entity.position(), Mod.sounds.voidBlossomSpike, SoundSource.HOSTILE, 1.2f, range = 64.0) },
            firstBurstDelay
        )
        createSpikeWave(
            {
                createBurst(spikeGenerator, secondCirclePoints)
                playSoundsInRadius(world, secondRadius, Mod.sounds.spikeWaveIndicator, 2.0f, 0.7f)
            },
            { playSoundsInRadius(world, secondRadius, Mod.sounds.voidBlossomSpike, 1.2f, entity.random.randomPitch()) },
            secondBurstDelay
        )
        createSpikeWave(
            {
                createBurst(spikeGenerator, thirdCirclePoints)
                playSoundsInRadius(world, thirdRadius, Mod.sounds.spikeWaveIndicator, 2.0f, 0.7f)
            },
            { playSoundsInRadius(world, thirdRadius, Mod.sounds.voidBlossomSpike, 1.2f, entity.random.randomPitch()) },
            thirdBurstDelay
        )
    }

    private fun createSpikeWave(
        indicationStageHandler: () -> Unit,
        spikeStageHandler: () -> Unit,
        burstDelay: Int
    ) {
        eventScheduler.addEvent(TimedEvent({
            indicationStageHandler()
            eventScheduler.addEvent(TimedEvent(spikeStageHandler, indicatorDelay, shouldCancel = shouldCancel))
        }, burstDelay, shouldCancel = shouldCancel))
    }

    private fun playSoundsInRadius(
        world: ServerLevel,
        radius: Double,
        soundEvent: SoundEvent,
        volume: Float,
        pitch: Float
    ) {
        for (dir in Direction.Plane.HORIZONTAL) {
            val pos = entity.position().add(Vec3(dir.step()).scale(radius))
            world.playSound(pos, soundEvent, SoundSource.HOSTILE, volume, pitch, 64.0)
        }
    }

    private fun createBurst(
        spikeGenerator: Spikes,
        positions: List<Vec3>
    ) {
        val placedPositions = positions.flatMap { spikeGenerator.tryPlaceRift(entity.position().add(it)) }.toList()

        eventScheduler.addEvent(TimedEvent({
            entity.sendSpikePacket(placedPositions)
        }, indicatorDelay, shouldCancel = shouldCancel))
    }

    companion object {
        const val indicatorDelay = 30
    }
}