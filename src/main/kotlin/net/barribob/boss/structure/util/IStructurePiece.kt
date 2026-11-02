package net.barribob.boss.structure.util

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.world.level.WorldGenLevel

interface IStructurePiece {
    fun addBlock(world: WorldGenLevel, block: BlockState, pos: BlockPos, box: BoundingBox)
}