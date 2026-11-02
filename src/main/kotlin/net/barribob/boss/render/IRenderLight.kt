package net.barribob.boss.render

import net.minecraft.world.entity.Entity
import net.minecraft.core.BlockPos

interface IRenderLight<T : Entity> {
    fun getBlockLight(entity: T, blockPos: BlockPos): Int
}