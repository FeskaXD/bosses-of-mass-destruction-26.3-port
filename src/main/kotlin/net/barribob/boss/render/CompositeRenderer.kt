package net.barribob.boss.render

import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.Entity

class CompositeRenderer<T : Entity>(vararg renderers: IRenderer<T>) : IRenderer<T> {
    var rendererList = renderers.toList()

    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
    ) {
        rendererList.forEach { it.render(entity, yaw, partialTicks, matrices, vertexConsumers, light) }
    }
}