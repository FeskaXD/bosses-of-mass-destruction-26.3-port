package net.barribob.boss.mob

import com.geckolib.animatable.GeoAnimatable
import com.geckolib.renderer.base.GeoRenderState
import net.barribob.boss.mob.utils.GeoTickets
import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.barribob.boss.render.ITextureProvider
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.resources.Identifier
import com.geckolib.model.GeoModel as GeckoModel

class GeoModel<T>(
    private val modelLocation: (T) -> Identifier,
    private val textureProvider: ITextureProvider<T>,
    private val animationLocation: Identifier,
    val codeAnimations: ICodeAnimations<T> = ICodeAnimations { _, _, _ -> },
    val renderLayer: (Identifier) -> RenderType = { RenderTypes.entityCutout(it) }
) : GeckoModel<T>() where T : GeoAnimatable {
    // GeckoLib 5 resolves resources from the render state; the entity is stored there by addAdditionalStateData.
    private fun entity(state: GeoRenderState): T = GeoTickets.entity<T>(state)
        ?: throw IllegalStateException("Bosses of Mass Destruction: render state has no entity")

    override fun addAdditionalStateData(animatable: T, relatedObject: Any?, renderState: GeoRenderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState)
        renderState.addGeckolibData(GeoTickets.ENTITY, animatable)
    }

    override fun getModelResource(renderState: GeoRenderState): Identifier = modelLocation(entity(renderState))
    override fun getTextureResource(renderState: GeoRenderState): Identifier = textureProvider.getTexture(entity(renderState))
    override fun getAnimationResource(animatable: T): Identifier = animationLocation
}
