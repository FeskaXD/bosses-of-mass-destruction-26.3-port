package net.barribob.boss.particle

import net.minecraft.util.ARGB
import org.joml.Vector3f
import org.joml.Matrix3f
import org.joml.Quaternionf
import net.minecraft.client.renderer.state.level.QuadParticleRenderState
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.coerceAtLeast
import net.barribob.maelstrom.static_utilities.coerceAtMost
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SingleQuadParticle
import net.minecraft.client.Camera
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

class SimpleParticle(
    private val particleContext: ParticleContext,
    particleAge: Int,
    private val particleGeometry: IParticleGeometry,
    private val cycleSprites: Boolean = true,
    doCollision: Boolean = true
) :
    SingleQuadParticle(
        particleContext.world,
        particleContext.pos.x,
        particleContext.pos.y,
        particleContext.pos.z,
        particleContext.spriteProvider.first()
    ) {

    private var brightnessOverride: ((Float) -> Int)? = null
    private var colorOverride: ((Float) -> Vec3)? = null
    private var scaleOverride: ((Float) -> Float)? = null
    private var colorVariation: Vec3 = Vec3.ZERO
    private var velocityOverride: ((SimpleParticle) -> Vec3)? = null
    private var positionOverride: ((SimpleParticle) -> Vec3)? = null
    private var rotationOverride: ((SimpleParticle) -> Float)? = null

    private var rotation = 0f
    private var prevRotation = 0f
    var ageRatio = 1f
        private set

    override fun getLayer(): SingleQuadParticle.Layer = SingleQuadParticle.Layer.OPAQUE

    fun getPos(): Vec3{
        return Vec3(x, y, z)
    }

    fun getAge(): Int = age

    override fun tick() {
        super.tick()
        if (isAlive) {
            if (cycleSprites) setSpriteFromAge(particleContext.spriteProvider)
            ageRatio = age / lifetime.toFloat()
            setColorFromOverride(colorOverride, ageRatio)
            setScaleFromOverride(scaleOverride, ageRatio)
            setVelocityFromOverride(velocityOverride)
            setPositionFromOverride(positionOverride)
            setRotationFromOverride(rotationOverride)
        }
    }

    private fun setRotationFromOverride(rotationOverride: ((SimpleParticle) -> Float)?) {
        if (rotationOverride != null) {
            val rot = rotationOverride(this)
            prevRotation = rotation
            rotation = rot
        }
    }

    private fun setVelocityFromOverride(velocityOverride: ((SimpleParticle) -> Vec3)?) {
        if (velocityOverride != null) {
            val velocity = velocityOverride(this)
            xd = velocity.x
            yd = velocity.y
            zd = velocity.z
        }
    }

    private fun setPositionFromOverride(positionOverride: ((SimpleParticle) -> Vec3)?) {
        if (positionOverride != null) {
            val pos = positionOverride(this)
            setPos(pos.x, pos.y, pos.z)
        }
    }

    private fun setScaleFromOverride(scaleOverride: ((Float) -> Float)?, ageRatio: Float) {
        if (scaleOverride != null) {
            quadSize = scaleOverride(ageRatio)
            setSize(0.2f * quadSize, 0.2f * quadSize)
        }
    }

    private fun setColorFromOverride(colorOverride: ((Float) -> Vec3)?, ageRatio: Float) {
        if (colorOverride != null) {
            val color = colorOverride(ageRatio)
            val variedColor = color.add(colorVariation).coerceAtLeast(Vec3.ZERO).coerceAtMost(VecUtils.unit)
            setColor(variedColor.x.toFloat(), variedColor.y.toFloat(), variedColor.z.toFloat())
        }
    }

    fun setBrightnessOverride(override: ((Float) -> Int)?) {
        brightnessOverride = override
    }

    fun setColorOverride(override: ((Float) -> Vec3)?) {
        colorOverride = override
        setColorFromOverride(override, 0f)
    }

    fun setScaleOverride(override: ((Float) -> Float)?) {
        scaleOverride = override
        setScaleFromOverride(override, 0f)
    }

    fun setColorVariation(variation: Double) {
        colorVariation = RandomUtils.randVec().scale(variation)
        setColorFromOverride(colorOverride, 0f)
    }

    fun setVelocityOverride(override: ((SimpleParticle) -> Vec3)?) {
        velocityOverride = override
    }

    fun setPositionOverride(override: ((SimpleParticle) -> Vec3)?) {
        positionOverride = override
    }

    fun setRotationOverride(override: ((SimpleParticle) -> Float)?) {
        rotationOverride = override
        rotationOverride?.let {
            rotation = it(this)
            prevRotation = it(this)
        }
    }

    override fun getLightCoords(tint: Float): Int =
        brightnessOverride?.invoke(ageRatio) ?: super.getLightCoords(tint)

    /**
     * 26.3 particles submit one rotated square per particle instead of raw vertices. BOMD's geometries still produce
     * four camera-relative corners (in the 1.21 vertex order); they are turned back into centre + rotation + half size.
     */
    override fun extract(state: QuadParticleRenderState, camera: Camera, tickDelta: Float) {
        val v = particleGeometry.getGeometry(
            camera,
            tickDelta,
            xo, yo, zo,
            x, y, z,
            getQuadSize(tickDelta),
            Mth.lerp(tickDelta, prevRotation, rotation)
        )
        val center = Vector3f(v[0]).add(v[1]).add(v[2]).add(v[3]).mul(0.25f)
        val xAxis = Vector3f(v[1]).sub(v[2])
        val halfSize = xAxis.length() * 0.5f
        if (halfSize < 1.0e-6f) return
        xAxis.normalize()
        val yAxis = Vector3f(v[1]).sub(v[0]).normalize()
        val zAxis = Vector3f(xAxis).cross(yAxis).normalize()
        yAxis.set(zAxis).cross(xAxis).normalize()
        val rotation = Quaternionf().setFromNormalized(Matrix3f(xAxis, yAxis, zAxis))
        state.add(
            layer, center.x, center.y, center.z,
            rotation.x, rotation.y, rotation.z, rotation.w, halfSize,
            u0, u1, v0, v1,
            ARGB.colorFromFloat(alpha, rCol, gCol, bCol), getLightCoords(tickDelta)
        )
    }

    init {
        this.lifetime = particleAge
        if (cycleSprites) setSpriteFromAge(particleContext.spriteProvider) else setSprite(particleContext.spriteProvider.first())
        xd = particleContext.vel.x
        yd = particleContext.vel.y
        zd = particleContext.vel.z
        hasPhysics = doCollision
    }
}