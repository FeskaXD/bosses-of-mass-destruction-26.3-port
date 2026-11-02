package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.render.IRenderer
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack

class LaserParticleRenderer: IRenderer<GauntletEntity> {
    override fun render(
        entity: GauntletEntity,
        yaw: Float,
        partialTicks: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int
    ) {
        if (entity.laserHandler.shouldRenderLaser()) {
            val beamPos = entity.laserHandler.getLaserRenderPos()
            val lerpedPos = MathUtils.lerpVec(partialTicks, beamPos.second, beamPos.first)
            val beamVel = MathUtils.unNormedDirection(beamPos.second, beamPos.first).normalize().scale(0.1)
            val laserDir = MathUtils.unNormedDirection(entity.eyePos(), lerpedPos)
            entity.laserHandler.laserChargeParticles.build(entity.eyePos().add(laserDir.scale(entity.random.nextDouble())), beamVel)
        }
    }
}