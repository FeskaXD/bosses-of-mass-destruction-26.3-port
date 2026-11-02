package net.barribob.boss.block

import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.IronBarsBlock
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource

class VineWallBlock(settings: BlockBehaviour.Properties) : IronBarsBlock(settings) {
    override fun tick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        world.destroyBlock(pos, false)
    }
}