package net.barribob.boss.mob.mobs.gauntlet

import net.minecraft.client.renderer.rendertype.RenderTypes
import net.barribob.boss.Mod
import net.barribob.boss.render.IRenderer
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.client.renderer.rendertype.RenderType
import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack

class GauntletLaserRenderer : IRenderer<GauntletEntity> {
    private val laserTexture = Mod.identifier("textures/entity/gauntlet_beam.png")
    private val layer: RenderType = RenderTypes.entityCutout(laserTexture)

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
            VanillaCopies.renderBeam(
                entity,
                beamPos.first,
                beamPos.second,
                partialTicks,
                ModColors.LASER_RED,
                matrices,
                vertexConsumers,
                layer
            )
        }
    }
}