package net.barribob.boss.projectile

import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.render.IRenderer
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

class PetalBladeParticleRenderer<T : Entity> : IRenderer<T> {
    private val petalParticleFactory = ClientParticleBuilder(Particles.PETAL)
        .color {
            if (it < 0.7) {
                MathUtils.lerpVec(it, ModColors.PINK, Vec3(1.0, 0.85, 0.95))
            } else {
                MathUtils.lerpVec(it, Vec3(1.0, 0.85, 0.95), ModColors.ULTRA_DARK_PURPLE)
            }
        }
        .brightness(Particles.FULL_BRIGHT)
        .colorVariation(0.15)
        .scale { RandomUtils.range(0.1, 0.2).toFloat() * (1 - it * 0.25f) }

    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
    ) {
        val prevPos = Vec3(entity.xo, entity.yo, entity.zo)
        val pos = MathUtils.lerpVec(partialTicks, prevPos, entity.position())
        val dir = entity.position().subtract(prevPos).normalize()

        val randomRot = RandomUtils.range(0, 360)
        val angularMomentum = RandomUtils.randSign() * 4f
        petalParticleFactory
            .continuousRotation { randomRot + it.getAge() * angularMomentum }
            .build(
                pos.add(RandomUtils.randVec().scale(0.25)),
                RandomUtils.randVec().planeProject(VecUtils.yAxis)
                    .subtract(VecUtils.yAxis).normalize()
                    .scale(0.1)
                    .add(dir.scale(0.05))
            )
    }
}