package net.barribob.boss.render

import com.geckolib.renderer.base.GeoRenderState
import com.geckolib.renderer.base.GeoRenderer
import com.geckolib.renderer.base.RenderPassInfo
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.resources.Identifier

/**
 * Re-submits the boss model with the vanilla energy-swirl render type, slightly enlarged (the old renderers scaled each
 * cube; GeckoLib 5 only lets us scale the whole pass). TODO(26.3 port): shield tint/alpha is not applied yet.
 */
object EnergyShieldLayer {
    @Suppress("UNCHECKED_CAST")
    fun submit(renderPassInfo: RenderPassInfo<*>, collector: SubmitNodeCollector, texture: Identifier, textureOffset: Float, scaleXZ: Float, scaleY: Float) {
        val info = renderPassInfo as RenderPassInfo<GeoRenderState>
        val renderer = info.renderer() as GeoRenderer<*, *, GeoRenderState>
        val pose = info.poseStack()
        pose.pushPose()
        pose.scale(scaleXZ, scaleY, scaleXZ)
        renderer.submitRenderTasks(info, collector.order(1), RenderTypes.energySwirl(texture, textureOffset, textureOffset))
        pose.popPose()
    }
}
