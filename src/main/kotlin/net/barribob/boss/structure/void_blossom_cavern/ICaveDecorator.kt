package net.barribob.boss.structure.void_blossom_cavern

import net.barribob.boss.structure.util.IStructurePiece
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.chunk.ChunkGenerator

interface ICaveDecorator {
    fun onBlockPlaced(pos: BlockPos, block: Block)
    fun generate(world: WorldGenLevel,
                 chunkGenerator: ChunkGenerator,
                 random: RandomSource,
                 boundingBox: BoundingBox,
                 pos: BlockPos,
                 structurePiece: IStructurePiece
    )
}