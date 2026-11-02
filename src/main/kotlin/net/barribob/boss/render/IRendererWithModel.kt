package net.barribob.boss.render

import com.geckolib.renderer.base.RenderPassInfo
import net.minecraft.client.renderer.SubmitNodeCollector

/** Extra pass over the entity's baked model (e.g. an energy shield), run as a GeckoLib render layer. */
fun interface IRendererWithModel<T> {
    fun render(entity: T, partialTicks: Float, renderPassInfo: RenderPassInfo<*>, collector: SubmitNodeCollector)
}
