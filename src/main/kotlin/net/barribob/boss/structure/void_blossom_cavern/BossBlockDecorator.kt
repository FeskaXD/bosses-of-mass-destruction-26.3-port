package net.barribob.boss.structure.void_blossom_cavern

import net.barribob.boss.block.ModBlocks
import net.barribob.boss.structure.util.IStructurePiece
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.chunk.ChunkGenerator

class BossBlockDecorator(private val bottomOfWorld: Int) : ICaveDecorator {
    override fun onBlockPlaced(pos: BlockPos, block: Block) {
    }

    override fun generate(
        world: WorldGenLevel,
        chunkGenerator: ChunkGenerator,
        random: RandomSource,
        boundingBox: BoundingBox,
        pos: BlockPos,
        structurePiece: IStructurePiece
    ) {
        structurePiece.addBlock(world, ModBlocks.voidBlossomSummonBlock.defaultBlockState(), bossBlockOffset(pos, bottomOfWorld), boundingBox)
    }

    companion object {
        fun bossBlockOffset(pos: BlockPos, bottomOfWorld: Int) = BlockPos(pos.x + 3, bottomOfWorld + 5, pos.z + 3)
    }
}