package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.render.IRenderer
import net.barribob.boss.render.legacy.MultiBufferSource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.multiplayer.ClientLevel

class EternalNightRenderer: IRenderer<LichEntity> {
    override fun render(
        entity: LichEntity,
        yaw: Float,
        partialTicks: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
    ) {
        val world = entity.level()
        val isAddedToWorld = world.getEntity(entity.id) != null
        if (entity.shouldSetToNighttime && world is ClientLevel && isAddedToWorld) {
            // 26.3 port: night is now set through the server world clock (LichEntity.serverTick), synced to clients
        }
    }
}