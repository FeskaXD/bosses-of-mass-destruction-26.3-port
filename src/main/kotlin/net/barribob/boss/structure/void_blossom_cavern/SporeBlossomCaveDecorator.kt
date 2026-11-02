package net.barribob.boss.structure.void_blossom_cavern

import com.mojang.datafixers.util.Pair
import net.barribob.boss.structure.util.IStructurePiece
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.core.Holder
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.placement.CountPlacement
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement

class SporeBlossomCaveDecorator(private val bottomOfWorld: Int, private val random: RandomSource) : ICaveDecorator {
    private val sporeBlossomPositions = mutableListOf<BlockPos>()

    override fun onBlockPlaced(pos: BlockPos, block: Block) {
        if (pos.y > 20 + bottomOfWorld && random.nextInt(20) == 0 && block != Blocks.AIR) {
            sporeBlossomPositions.add(pos)
        }
    }

    override fun generate(
        world: WorldGenLevel,
        chunkGenerator: ChunkGenerator,
        random: RandomSource,
        boundingBox: BoundingBox,
        pos: BlockPos,
        structurePiece: IStructurePiece
    ) {
        val spacedSporeBlossomPositions =
            sporeBlossomPositions.groupBy { Pair(it.x shr 3, it.z shr 3) }.map { it.value.first() }
        for (sporePos in spacedSporeBlossomPositions) {
            if (boundingBox.isInside(sporePos)) {
                placeSporeBlossoms(world, random, boundingBox, sporePos)
            }
        }
    }

    /**
     * 26.3 port: stands in for the old in-code placed feature (25 tries in the area, scan up to 12 blocks for a solid
     * ceiling, place a spore blossom underneath it).
     */
    private fun placeSporeBlossoms(world: WorldGenLevel, random: RandomSource, boundingBox: BoundingBox, origin: BlockPos) {
        repeat(25) {
            var pos = origin.offset(random.nextInt(16) - 8, random.nextInt(16) - 8, random.nextInt(16) - 8)
            if (!world.isEmptyBlock(pos)) return@repeat
            for (i in 0 until 12) {
                val above = pos.above()
                if (world.getBlockState(above).isSolid) {
                    if (boundingBox.isInside(pos) && Blocks.SPORE_BLOSSOM.defaultBlockState().canSurvive(world, pos)) {
                        world.setBlock(pos, Blocks.SPORE_BLOSSOM.defaultBlockState(), 2)
                    }
                    return@repeat
                }
                if (!world.isEmptyBlock(above)) return@repeat
                pos = above
            }
        }
    }
}