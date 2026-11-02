package net.barribob.boss.mob.mobs.obsidilith

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
import kotlin.random.Random

/** Energy shield shown while the Obsidilith is shielded. TODO(26.3 port): per-attack shield colour (was tinted by currentAttack). */
class ObsidilithArmorRenderer(@Suppress("unused") val geoModel: GeoModel<ObsidilithEntity>, @Suppress("unused") val context: EntityRendererProvider.Context) :
    IRendererWithModel<ObsidilithEntity>, IRenderer<ObsidilithEntity> {
    private val armorTexture = Mod.identifier("textures/entity/obsidilith_armor.png")

    override fun render(entity: ObsidilithEntity, partialTicks: Float, renderPassInfo: RenderPassInfo<*>, collector: SubmitNodeCollector) {
        if (!entity.isShielded()) return
        val textureOffset = (entity.tickCount + partialTicks) * Random.nextFloat()
        EnergyShieldLayer.submit(renderPassInfo, collector, armorTexture, textureOffset, 1.08f, 1.05f)
    }

    override fun render(entity: ObsidilithEntity, yaw: Float, partialTicks: Float, matrices: PoseStack, vertexConsumers: MultiBufferSource, light: Int) {
    }
}
