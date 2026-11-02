package net.barribob.boss.structure.void_blossom_cavern

import com.mojang.serialization.MapCodec
import net.barribob.boss.Mod
import net.barribob.boss.structure.util.CodeStructurePiece
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType
import java.util.*

class VoidBlossomArenaStructureFeature(codec: Structure.StructureSettings) : Structure(codec) {

    companion object {
        fun addPieces(collector : StructurePiecesBuilder, context : Structure.GenerationContext) {
            val x = context.chunkPos().minBlockX
            val z = context.chunkPos().minBlockZ
            val y = 35 + context.chunkGenerator().minY
            collector.addPiece(
                CodeStructurePiece(
                    Mod.structures.voidBlossomCavernPiece,
                    BoundingBox(BlockPos(x, y, z)).inflatedBy(32),
                    VoidBlossomCavernPieceGenerator()
                )
            )
        }
        
        val CODEC: MapCodec<VoidBlossomArenaStructureFeature> = simpleCodec(::VoidBlossomArenaStructureFeature)
    }

    override fun findGenerationPoint(context: Structure.GenerationContext): Optional<Structure.GenerationStub> {
        return onTopOfChunkCenter(
            context, Heightmap.Types.WORLD_SURFACE_WG
        ) { collector: StructurePiecesBuilder? -> addPieces(collector as StructurePiecesBuilder, context) }
    }

    override fun type(): StructureType<*> {
        return Mod.structures.voidBlossomStructureType
    }
}