package net.barribob.boss.render

import net.barribob.boss.utils.VanillaCopies
import net.minecraft.client.renderer.rendertype.RenderType
import net.barribob.boss.render.legacy.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.Entity

class BillboardRenderer<T : Entity>(
    private val dispatcher: EntityRenderDispatcher,
    private val renderLayer: RenderType,
    private val scale: (T) -> Float,
) : IRenderer<T> {
    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
    ) {
        val scale = scale(entity)
        matrices.pushPose()
        matrices.scale(scale, scale, scale)
        VanillaCopies.renderBillboard(matrices, vertexConsumers, light, dispatcher, renderLayer)
        matrices.popPose()
    }
}