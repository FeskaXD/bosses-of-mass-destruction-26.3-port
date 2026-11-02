package net.barribob.boss.render

import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.Entity
import java.util.function.Predicate

class ConditionalRenderer<T : Entity>(private val predicate: Predicate<T>, private val renderer: IRenderer<T>) : IRenderer<T> {
    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
    ) {
        if (predicate.test(entity)) {
            renderer.render(entity, yaw, partialTicks, matrices, vertexConsumers, light)
        }
    }
}