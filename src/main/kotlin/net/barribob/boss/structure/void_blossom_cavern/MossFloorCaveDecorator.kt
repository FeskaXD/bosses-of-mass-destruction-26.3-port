package net.barribob.boss.structure.void_blossom_cavern

import com.mojang.datafixers.util.Pair
import net.barribob.boss.structure.util.IStructurePiece
import net.barribob.boss.utils.ModUtils.getConfiguredFeature
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.data.worldgen.features.CaveFeatures

import java.util.*

class MossFloorCaveDecorator(private val bottomOfWorld: Int, private val random: RandomSource) : ICaveDecorator {
    private val mossFloorPositions = mutableListOf<BlockPos>()

    override fun onBlockPlaced(pos: BlockPos, block: Block) {
        if (pos.y == 4 + bottomOfWorld && random.nextInt(80) == 0) {
            mossFloorPositions.add(pos)
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
        val spacedMossPositions = mossFloorPositions.groupBy { Pair(it.x shr 3, it.z shr 3) }.map { it.value.first() }
        for (mossPos in spacedMossPositions) {
            if (boundingBox.isInside(mossPos)) {
                world.getConfiguredFeature(CaveFeatures.MOSS_PATCH).place(world, chunkGenerator, random, mossPos)
            }
        }
    }
}