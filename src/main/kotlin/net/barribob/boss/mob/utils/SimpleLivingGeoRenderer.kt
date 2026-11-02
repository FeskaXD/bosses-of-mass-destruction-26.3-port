package net.barribob.boss.mob.utils

import com.geckolib.animatable.GeoAnimatable
import com.geckolib.constant.dataticket.DataTicket
import com.geckolib.renderer.GeoEntityRenderer
import com.geckolib.renderer.base.BoneSnapshots
import com.geckolib.renderer.base.GeoRenderState
import com.geckolib.renderer.base.RenderPassInfo
import com.geckolib.renderer.layer.GeoRenderLayer
import com.mojang.blaze3d.vertex.PoseStack
import net.barribob.boss.mob.GeoModel
import net.barribob.boss.render.*
import net.barribob.boss.render.legacy.CapturingBufferSource
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity

/** Render-state keys used to reach the entity from GeckoLib 5's render states. */
object GeoTickets {
    val ENTITY: DataTicket<Any> = DataTicket.create("bosses_of_mass_destruction_animatable", Any::class.java)

    @Suppress("UNCHECKED_CAST")
    fun <T> entity(state: GeoRenderState): T? = state.getGeckolibData(ENTITY) as T?
}

/**
 * GeckoLib 5 / MC 26.3 version of BOMD's entity renderer. Extra immediate-mode renderers ([IRenderer]) draw into a
 * [CapturingBufferSource] that is replayed through the submit pipeline; model overlays ([IRendererWithModel]) run as a
 * render layer. TODO(26.3 port): [iBoneLight] per-bone light/colour is not applied yet (GeckoLib 5 has no hook for it).
 */
class SimpleLivingGeoRenderer<T>(
    renderManager: EntityRendererProvider.Context,
    private val modelProvider: GeoModel<T>,
    private val brightness: IRenderLight<T>? = null,
    @Suppress("unused") private val iBoneLight: IBoneLight? = null,
    private val renderer: IRenderer<T>? = null,
    renderWithModel: IRendererWithModel<T>? = null,
    private val overlayOverride: IOverlayOverride? = null,
    private val deathRotation: Boolean = true
) : GeoEntityRenderer<T, EntityRenderState>(renderManager, modelProvider) where T : GeoAnimatable, T : Entity {

    init {
        if (renderWithModel != null) withRenderLayer(ModelLayer(this, renderWithModel))
    }

    override fun getBlockLightLevel(entity: T, blockPos: BlockPos): Int {
        return brightness?.getBlockLight(entity, blockPos) ?: super.getBlockLightLevel(entity, blockPos)
    }

    override fun addRenderData(animatable: T, relatedObject: Void?, renderState: EntityRenderState, partialTick: Float) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick)
        renderState.addGeckolibData(GeoTickets.ENTITY, animatable)
    }

    override fun getRenderType(renderState: EntityRenderState, texture: Identifier): RenderType =
        modelProvider.renderLayer(texture)

    override fun submit(state: EntityRenderState, poseStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
        val entity = GeoTickets.entity<T>(state)
        if (entity != null && renderer != null) {
            val partialTick = state.partialTick
            val buffers = CapturingBufferSource(collector)
            renderer.render(entity, entity.getViewYRot(partialTick), partialTick, poseStack, buffers, state.packedLight)
            buffers.flush()
        }
        poseStack.pushPose()
        super.submit(state, poseStack, collector, camera)
        poseStack.popPose()
    }

    override fun adjustModelBonesForRender(renderPassInfo: RenderPassInfo<EntityRenderState>, snapshots: BoneSnapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots)
        val state = renderPassInfo.renderState()
        val entity = GeoTickets.entity<T>(state) ?: return
        modelProvider.codeAnimations.animate(entity, state.partialTick, snapshots)
    }

    override fun getPackedOverlay(animatable: T, relatedObject: Void?, u: Float, partialTick: Float): Int {
        return overlayOverride?.getOverlay() ?: super.getPackedOverlay(animatable, relatedObject, u, partialTick)
    }

    override fun getDeathMaxRotation(renderState: GeoRenderState): Float = if (deathRotation) 90f else 0f

    private class ModelLayer<T>(
        renderer: SimpleLivingGeoRenderer<T>,
        private val model: IRendererWithModel<T>
    ) : GeoRenderLayer<T, Void, EntityRenderState>(renderer) where T : GeoAnimatable, T : Entity {
        override fun submitRenderTask(renderPassInfo: RenderPassInfo<EntityRenderState>, collector: SubmitNodeCollector) {
            val state = renderPassInfo.renderState()
            val entity = GeoTickets.entity<T>(state) ?: return
            model.render(entity, state.partialTick, renderPassInfo, collector)
        }
    }
}
