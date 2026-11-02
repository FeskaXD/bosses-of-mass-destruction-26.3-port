package net.barribob.boss.particle

import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.world.phys.Vec3

class SimpleParticleFactory(
    private val spriteProvider: SpriteSet,
    private val particleFactory: (ParticleContext) -> Particle
) : ParticleProvider<SimpleParticleType> {
    override fun createParticle(
        parameters: SimpleParticleType,
        world: ClientLevel,
        x: Double,
        y: Double,
        z: Double,
        velocityX: Double,
        velocityY: Double,
        velocityZ: Double,
        random: net.minecraft.util.RandomSource
    ): Particle {
        return particleFactory(
            ParticleContext(
                spriteProvider,
                world,
                Vec3(x, y, z),
                Vec3(velocityX, velocityY, velocityZ)
            )
        )
    }
}