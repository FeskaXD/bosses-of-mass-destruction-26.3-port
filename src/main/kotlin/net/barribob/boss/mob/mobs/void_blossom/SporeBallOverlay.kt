package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.projectile.SporeBallProjectile
import net.barribob.boss.render.IOverlayOverride
import net.barribob.boss.render.IRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import kotlin.math.pow

class SporeBallOverlay : IRenderer<SporeBallProjectile>, IOverlayOverride {
    private var entity: SporeBallProjectile? = null
    private var partialTicks: Float? = null

    override fun render(
        entity: SporeBallProjectile,
        yaw: Float,
        partialTicks: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int
    ) {
        this.entity = entity
        this.partialTicks = partialTicks
    }

    override fun getOverlay(): Int {
        val entity = entity ?: return OverlayTexture.pack(OverlayTexture.u(0f), OverlayTexture.v(false))
        val partialTicks = partialTicks ?: 0f
        val deathProgress: Float = if (!entity.impacted || entity.isRemoved) {
            0f
        } else {
            (entity.impactedTicks + partialTicks) / SporeBallProjectile.explosionDelay
        }
        val scaledDeathProgress = deathProgress.pow(2f)
        return OverlayTexture.pack(OverlayTexture.u(scaledDeathProgress), OverlayTexture.v(false))
    }
}