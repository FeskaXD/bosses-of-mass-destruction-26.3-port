package net.barribob.boss.render

import com.mojang.blaze3d.vertex.PoseStack
import net.barribob.boss.render.legacy.CapturingBufferSource
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity

/** 26.3 render-state version: the state keeps the entity so BOMD's [IRenderer]s can keep drawing from it. */
class SimpleEntityRenderer<T : Entity>(
    renderManager: EntityRendererProvider.Context,
    private val renderer: IRenderer<T>,
    @Suppress("unused") private val textureProvider: ITextureProvider<T>,
    private val brightness: IRenderLight<T>? = null,
) : EntityRenderer<T, SimpleEntityRenderer.State>(renderManager) {
    class State : EntityRenderState() {
        var entity: Entity? = null
        var yaw = 0f
        var tickDelta = 0f
    }

    override fun createRenderState() = State()

    override fun extractRenderState(entity: T, state: State, partialTick: Float) {
        super.extractRenderState(entity, state, partialTick)
        state.entity = entity
        state.yaw = entity.getViewYRot(partialTick)
        state.tickDelta = partialTick
    }

    override fun submit(state: State, poseStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
        @Suppress("UNCHECKED_CAST")
        val entity = state.entity as T? ?: return
        val buffers = CapturingBufferSource(collector)
        renderer.render(entity, state.yaw, state.tickDelta, poseStack, buffers, state.lightCoords)
        buffers.flush()
        super.submit(state, poseStack, collector, camera)
    }

    override fun getBlockLightLevel(entity: T, blockPos: BlockPos): Int =
        brightness?.getBlockLight(entity, blockPos) ?: super.getBlockLightLevel(entity, blockPos)
}
