package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.Entity
import net.minecraft.server.level.ServerLevel

class LightBlockPlacer(private val entity: Entity) : IEntityTick<ServerLevel> {
    override fun tick(world: ServerLevel) {
        if (world.getBlockState(entity.blockPosition()) != Blocks.LIGHT) {
            world.setBlock(entity.blockPosition(), Blocks.LIGHT.defaultBlockState(), 3)
        }
    }
}