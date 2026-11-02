package net.barribob.boss.structure.util

import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.chunk.ChunkGenerator

interface IPieceGenerator {
    fun generate(
        world: WorldGenLevel,
        structureAccessor: StructureManager,
        chunkGenerator: ChunkGenerator,
        random: RandomSource,
        boundingBox: BoundingBox,
        chunkPos: ChunkPos,
        pos: BlockPos,
        structurePiece: IStructurePiece
    ): Boolean
}