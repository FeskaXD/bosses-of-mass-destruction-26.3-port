package net.barribob.boss.structure.util

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext
import net.minecraft.world.level.levelgen.structure.StructurePiece
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.core.Direction
import net.minecraft.world.level.levelgen.LegacyRandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.chunk.ChunkGenerator

class CodeStructurePiece : StructurePiece, IStructurePiece {
    private val pieceGenerator: IPieceGenerator

    constructor(type: StructurePieceType, boundingBox: BoundingBox, structurePieceData: IPieceGenerator) : super(type, 0, boundingBox) {
        this.pieceGenerator = structurePieceData
        setOrientation(Direction.NORTH)
    }

    constructor(type: StructurePieceType, nbt: CompoundTag, structurePieceData: IPieceGenerator) : super(type, nbt) {
        this.pieceGenerator = structurePieceData
        setOrientation(Direction.NORTH)
    }

    override fun addAdditionalSaveData(context: StructurePieceSerializationContext, nbt: CompoundTag) {
    }

    override fun postProcess(world: WorldGenLevel, structureAccessor: StructureManager, chunkGenerator: ChunkGenerator, random: net.minecraft.util.RandomSource, chunkBox: BoundingBox, chunkPos: ChunkPos, pos: BlockPos) {
        pieceGenerator.generate(
            world,
            structureAccessor,
            chunkGenerator,
            LegacyRandomSource(world.seed),
            chunkBox,
            chunkPos,
            pos,
            this
        )
    }

    override fun addBlock(world: WorldGenLevel, block: BlockState, pos: BlockPos, box: BoundingBox) {
        placeBlock(world, block, pos.x, pos.y, pos.z, box)
    }

    // the pieces are generated in world coordinates: no rotation or mirroring
    override fun getRotation(): Rotation = Rotation.NONE
    override fun getMirror(): Mirror = Mirror.NONE
}