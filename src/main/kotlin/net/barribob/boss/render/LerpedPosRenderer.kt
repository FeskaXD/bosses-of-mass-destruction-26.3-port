package net.barribob.boss.render

import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.Entity
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

class LerpedPosRenderer <T : Entity> (val callback: (Vec3) -> Unit) : IRenderer<T> {
    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
    ) {
        val t = partialTicks.toDouble()
        val x = Mth.lerp(t, entity.xo, entity.x)
        val y = Mth.lerp(t, entity.yo, entity.y)
        val z = Mth.lerp(t, entity.zo, entity.z)

        callback(Vec3(x, y, z))
    }
}