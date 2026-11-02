package net.barribob.boss.render

import com.geckolib.animatable.GeoAnimatable
import com.geckolib.model.GeoModel
import com.geckolib.renderer.GeoBlockRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.entity.BlockEntity

/** GeckoLib 5 block entity renderer. TODO(26.3 port): [iBoneLight] (full-bright bones) is not applied yet. */
class ModBlockEntityRenderer<T>(
    context: BlockEntityRendererProvider.Context,
    modelProvider: GeoModel<T>,
    @Suppress("unused") private val iBoneLight: IBoneLight? = null,
) : GeoBlockRenderer<T, BlockEntityRenderState>(context, modelProvider) where T : BlockEntity, T : GeoAnimatable {
    override fun getRenderType(renderState: BlockEntityRenderState, texture: Identifier): RenderType =
        RenderTypes.entityCutout(texture)
}
