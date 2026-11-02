package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.render.IOverlayOverride
import net.barribob.boss.render.IRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import kotlin.math.sin

class GauntletOverlay : IRenderer<GauntletEntity>, IOverlayOverride {
    private var entity: GauntletEntity? = null
    private var partialTicks: Float? = null

    override fun render(
        entity: GauntletEntity,
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
        val deathProgress: Float = if (entity.deathTime == 0) {
            0f
        } else {
            (entity.deathTime + partialTicks) / ServerGauntletDeathHandler.deathAnimationTime
        }
        val flash = sin(deathProgress.toDouble() * 50).toFloat() * 0.1f + deathProgress * 0.9f
        return OverlayTexture.pack(OverlayTexture.u(flash), OverlayTexture.v(entity.hurtTime > 0))
    }
}