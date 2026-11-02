package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.projectile.SporeBallProjectile
import net.barribob.boss.render.IRenderer
import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import kotlin.math.pow

class SporeBallSizeRenderer : IRenderer<SporeBallProjectile> {
    override fun render(
        entity: SporeBallProjectile,
        yaw: Float,
        partialTicks: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int
    ) {
        val deathProgress: Float = if (!entity.impacted || entity.isRemoved) {
            0f
        } else {
            ((entity.impactedTicks + partialTicks) / SporeBallProjectile.explosionDelay) * 0.5f
        }
        val scaledDeathProgress = deathProgress.pow(2f) + 1
        matrices.scale(scaledDeathProgress, scaledDeathProgress, scaledDeathProgress)
    }
}