package net.barribob.boss.render

import net.minecraft.world.entity.Entity
import net.minecraft.core.BlockPos

class FullRenderLight<T : Entity> : IRenderLight<T> {
    override fun getBlockLight(entity: T, blockPos: BlockPos): Int = 15
}