package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.render.IOverlayOverride
import net.barribob.boss.render.IRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack

class NoRedOnDeathOverlay: IRenderer<VoidBlossomEntity>, IOverlayOverride {
    private var entity: VoidBlossomEntity? = null

    override fun render(
        entity: VoidBlossomEntity,
        yaw: Float,
        partialTicks: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int
    ) {
        this.entity = entity
    }

    override fun getOverlay(): Int {
        val entity = entity ?: return OverlayTexture.pack(OverlayTexture.u(0f), OverlayTexture.v(false))

        return OverlayTexture.pack(OverlayTexture.u(0f), OverlayTexture.v(entity.hurtTime > 0 && !entity.isDeadOrDying))
    }
}