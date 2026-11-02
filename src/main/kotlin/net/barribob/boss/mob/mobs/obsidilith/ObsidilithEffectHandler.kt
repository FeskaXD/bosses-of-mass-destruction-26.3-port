package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import kotlin.math.sin

class ObsidilithEffectHandler(val entity: LivingEntity, val eventScheduler: EventScheduler) {
    private val burstParticleFactory = ClientParticleBuilder(Particles.ENCHANT)
        .color(ModColors.ORANGE)
        .colorVariation(0.2)

    private val waveParticleFactory = ClientParticleBuilder(Particles.ENCHANT)
        .color(ModColors.RED)
        .colorVariation(0.2)

    private val spikeParticleFactory = ClientParticleBuilder(Particles.ENCHANT)
        .color(ModColors.COMET_BLUE)
        .colorVariation(0.2)

    private val anvilParticleFactory = ClientParticleBuilder(Particles.ENCHANT)
        .color(ModColors.ENDER_PURPLE)
        .colorVariation(0.2)

    private val teleportFactory = ClientParticleBuilder(Particles.DOWNSPARKLE)
        .color(ModColors.ENDER_PURPLE)
        .brightness(Particles.FULL_BRIGHT)
        .age { RandomUtils.range(25, 30) }
        .colorVariation(0.2)

    fun handleStatus(status: Byte) {
        when (status) {
            ObsidilithUtils.burstAttackStatus -> burstEffect()
            ObsidilithUtils.waveAttackStatus -> waveEffect()
            ObsidilithUtils.spikeAttackStatus -> spikeEffect()
            ObsidilithUtils.anvilAttackStatus -> anvilEffect()
            ObsidilithUtils.deathStatus -> deathEffect()
        }
    }

    private fun burstEffect() {
        val entityPos = entity.eyePos()
        for (i in 0..50) {
            val pos = entityPos.add(RandomUtils.randVec().normalize().scale(3.0))
            val vel = MathUtils.unNormedDirection(pos, entityPos).cross(VecUtils.yAxis).scale(0.1)
            burstParticleFactory.build(pos, vel)
        }
    }

    private fun waveEffect() {
        val entityPos = entity.position()
        for (i in 0..50) {
            val randomYOffset = VecUtils.yAxis.scale(entity.random.nextDouble())
            val randomYVel = VecUtils.yAxis.scale(entity.random.nextDouble())
            val pos = entityPos.add(
                RandomUtils.randVec()
                    .planeProject(VecUtils.yAxis)
                    .normalize().scale(3.0)
            )
                .add(randomYOffset)

            waveParticleFactory.continuousVelocity {
                MathUtils.unNormedDirection(it.getPos(), entityPos)
                    .cross(VecUtils.yAxis).reverse()
                    .add(randomYVel).scale(0.1)
            }.build(pos)
        }
    }

    private fun spikeEffect() {
        val entityPos = entity.position()
        for (i in 0..50) {
            val randomYOffset = VecUtils.yAxis.scale(entity.random.nextDouble())
            val randomYVel = VecUtils.yAxis.scale(entity.random.nextDouble())
            val pos = entityPos.add(
                RandomUtils.randVec()
                    .planeProject(VecUtils.yAxis)
                    .normalize().scale(3.0)
            )
                .add(randomYOffset)

            spikeParticleFactory.continuousVelocity {
                MathUtils.unNormedDirection(it.getPos(), entityPos)
                    .cross(VecUtils.yAxis)
                    .add(randomYVel).scale(0.1)
            }.build(pos)
        }
    }

    private fun anvilEffect() {
        val entityPos = entity.eyePos()
        for (i in 0..50) {
            val pos = entityPos.add(
                RandomUtils.randVec()
                    .planeProject(VecUtils.yAxis)
                    .normalize().scale(3.0)
            )

            anvilParticleFactory.continuousVelocity {
                MathUtils.unNormedDirection(it.getPos(), entityPos)
                    .cross(VecUtils.yAxis)
                    .add(VecUtils.yAxis.scale(0.4)).scale(0.1)
            }.build(pos)
        }

        eventScheduler.addEvent(TimedEvent({
            val particlePos = entity.position().add(RandomUtils.randVec().scale(3.0))
            val velocity = entity.deltaMovement.scale(0.7)
            teleportFactory.build(particlePos, velocity)
        }, 0, 80, { !entity.isAlive }))
    }

    private fun deathEffect() {
        val entityPos = entity.blockPosition().asVec3d().add(0.5, 0.5, 0.5)
        spawnPillarParticles(entityPos, eventScheduler)
    }

    companion object {
        private val deathParticleFactory = ClientParticleBuilder(Particles.DOWNSPARKLE)
            .color { age -> MathUtils.lerpVec(age, ModColors.ENDER_PURPLE, ModColors.WHITE) }
            .colorVariation(0.2)
            .brightness(Particles.FULL_BRIGHT)
            .age { RandomUtils.range(35, 40) }
            .scale { (sin(it.toDouble() * Math.PI * 0.5) + 1f).toFloat() * 0.1f }

        fun spawnPillarParticles(entityPos: Vec3, eventScheduler: EventScheduler) {
            for (i in 0..ObsidilithUtils.deathPillarHeight) {
                eventScheduler.addEvent(TimedEvent({
                    MathUtils.circleCallback(3.0, 30, VecUtils.yAxis) {
                        deathParticleFactory.build(entityPos.add(it).add(VecUtils.yAxis.scale(i.toDouble())))
                    }
                }, i * ObsidilithUtils.ticksBetweenPillarLayer))
            }
        }
    }
}