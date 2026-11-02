package net.barribob.boss.mob.mobs.gauntlet

import com.geckolib.renderer.base.RenderPassInfo
import net.barribob.boss.Mod
import net.barribob.boss.mob.GeoModel
import net.barribob.boss.render.EnergyShieldLayer
import net.barribob.boss.render.IRenderer
import net.barribob.boss.render.IRendererWithModel
import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider

/** Gauntlet energy shield. TODO(26.3 port): the red tint and fade-in alpha are not applied yet. */
class GauntletEnergyRenderer(@Suppress("unused") val geoModel: GeoModel<GauntletEntity>, @Suppress("unused") val context: EntityRendererProvider.Context) :
    IRendererWithModel<GauntletEntity>, IRenderer<GauntletEntity> {
    private val armorTexture = Mod.identifier("textures/entity/obsidilith_armor.png")

    override fun render(entity: GauntletEntity, partialTicks: Float, renderPassInfo: RenderPassInfo<*>, collector: SubmitNodeCollector) {
        if (entity.energyShieldHandler.getRenderAlpha() == 0f) return
        val textureOffset = (entity.tickCount + partialTicks) * 0.01f
        EnergyShieldLayer.submit(renderPassInfo, collector, armorTexture, textureOffset, 1.1f, 1.05f)
    }

    override fun render(entity: GauntletEntity, yaw: Float, partialTicks: Float, matrices: PoseStack, vertexConsumers: MultiBufferSource, light: Int) {
    }
}
