package net.barribob.boss.projectile

import net.barribob.boss.render.IRenderer
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.client.renderer.rendertype.RenderType
import net.barribob.boss.render.legacy.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis


class PetalBladeRenderer(
    private val dispatcher: EntityRenderDispatcher,
    private val renderLayer: RenderType,
) : IRenderer<PetalBladeProjectile> {
    override fun render(
        entity: PetalBladeProjectile,
        yaw: Float,
        partialTicks: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
    ) {
        val scale = 0.5f
        matrices.pushPose()
        matrices.scale(scale, scale, scale)
        VanillaCopies.renderBillboard(matrices, vertexConsumers, light, dispatcher, renderLayer, Axis.ZP.rotationDegrees(-entity.entityData.get(PetalBladeProjectile.renderRotation)))
        matrices.popPose()
    }
}