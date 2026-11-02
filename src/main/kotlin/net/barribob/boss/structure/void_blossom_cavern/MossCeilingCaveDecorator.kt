package net.barribob.boss.structure.void_blossom_cavern

import com.mojang.datafixers.util.Pair
import net.barribob.boss.structure.util.IStructurePiece
import net.barribob.boss.utils.ModUtils.getConfiguredFeature
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.data.worldgen.features.CaveFeatures

class MossCeilingCaveDecorator(private val bottomOfWorld: Int, private val random: RandomSource) : ICaveDecorator {
    private val mossCeilingPositions = mutableListOf<BlockPos>()

    override fun onBlockPlaced(pos: BlockPos, block: Block) {
        if (pos.y > 18 + bottomOfWorld && random.nextInt(20) == 0 && block != Blocks.AIR) {
            mossCeilingPositions.add(pos)
        }
    }

    override fun generate(world: WorldGenLevel, chunkGenerator: ChunkGenerator, random: RandomSource, boundingBox: BoundingBox, pos: BlockPos, structurePiece: IStructurePiece) {
        val spacedMossCeilingPositions =
            mossCeilingPositions.groupBy { Pair(it.x shr 3, it.z shr 3) }.map { it.value.first() }
        for (mossPos in spacedMossCeilingPositions) {
            if (boundingBox.isInside(mossPos)) {
                world.getConfiguredFeature(CaveFeatures.MOSS_PATCH_CEILING).place(world, chunkGenerator, random, mossPos)
            }
        }
    }
}