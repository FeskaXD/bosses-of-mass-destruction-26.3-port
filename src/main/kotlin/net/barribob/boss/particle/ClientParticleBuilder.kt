package net.barribob.boss.particle

import net.barribob.maelstrom.static_utilities.RandomUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.Camera
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.phys.Vec3

class ClientParticleBuilder(private val effect: ParticleOptions) {
    private var getVel: ((SimpleParticle) -> Vec3)? = null
    private var continuousPos: ((SimpleParticle) -> Vec3)? = null
    private var color: ((Float) -> Vec3)? = null
    private var brightness: ((Float) -> Int)? = null
    private var scale: ((Float) -> Float)? = null
    private var age: (() -> Int)? = null
    private var colorVariation: Double? = null
    private var getRotation: ((SimpleParticle) -> Float)? = null

    fun continuousRotation(rotation: (SimpleParticle) -> Float): ClientParticleBuilder {
        this.getRotation = rotation
        return this
    }

    fun continuousVelocity(velocity: (SimpleParticle) -> Vec3): ClientParticleBuilder {
        this.getVel = velocity
        return this
    }

    fun continuousPosition(positionProvider: (SimpleParticle) -> Vec3): ClientParticleBuilder {
        this.continuousPos = positionProvider
        return this
    }

    fun color(color: ((Float) -> Vec3)?): ClientParticleBuilder {
        this.color = color
        return this
    }

    fun color(color: Vec3): ClientParticleBuilder {
        this.color = { color }
        return this
    }

    fun rotation(rotation: Float): ClientParticleBuilder {
        this.getRotation = { rotation }
        return this
    }

    fun brightness(brightness: Int): ClientParticleBuilder {
        this.brightness = { brightness }
        return this
    }

    fun scale(scale: ((Float) -> Float)?): ClientParticleBuilder {
        this.scale = scale
        return this
    }

    fun scale(scale: Float): ClientParticleBuilder {
        this.scale = { scale }
        return this
    }

    fun age(age: (() -> Int)?): ClientParticleBuilder {
        this.age = age
        return this
    }

    fun age(age: Int): ClientParticleBuilder {
        this.age = { age }
        return this
    }

    fun age(min: Int, max: Int): ClientParticleBuilder {
        this.age = { RandomUtils.range(min, max) }
        return this
    }

    fun colorVariation(variation: Double): ClientParticleBuilder {
        this.colorVariation = variation
        return this
    }

    fun build(pos: Vec3, vel: Vec3 = Vec3.ZERO) {
        val client = Minecraft.getInstance()
        val camera: Camera = client.gameRenderer.mainCamera()
        if (client != null && camera.isInitialized() && client.particleEngine != null) {
            val particle = client.particleEngine.createParticle(effect, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z)
                ?: return

            scale?.let { particle.scale(it(0f)) }
            color?.let {
                val clr = it(0f)
                (particle as? net.minecraft.client.particle.SingleQuadParticle)?.setColor(clr.x.toFloat(), clr.y.toFloat(), clr.z.toFloat())
            }
            age?.let { particle.lifetime = it() }

            if (particle is SimpleParticle) {
                brightness?.let { particle.setBrightnessOverride(brightness) }
                color?.let { particle.setColorOverride(color) }
                scale?.let { particle.setScaleOverride(scale) }
                getVel?.let { particle.setVelocityOverride(it) }
                continuousPos?.let { particle.setPositionOverride(it) }
                colorVariation?.let { particle.setColorVariation(it) }
                getRotation?.let { particle.setRotationOverride(it) }
            }
        }
    }
}