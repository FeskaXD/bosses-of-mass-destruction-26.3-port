package net.barribob.boss.utils

import net.barribob.boss.Mod
import net.barribob.boss.structure.util.CodeStructurePiece
import net.barribob.boss.structure.void_blossom_cavern.VoidBlossomArenaStructureFeature
import net.barribob.boss.structure.void_blossom_cavern.VoidBlossomCavernPieceGenerator
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
import net.minecraft.resources.Identifier
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType


class ModStructures {
    val obsidilithStructureRegistry = StructureRegister(Mod.identifier("obsidilith_arena"))
    val gauntletStructureRegistry = StructureRegister(Mod.identifier("gauntlet_arena"))
    val lichStructureRegistry = StructureRegister(Mod.identifier("lich_tower"))

    val soulStarStructureKey: TagKey<Structure> = TagKey.create(net.minecraft.core.registries.Registries.STRUCTURE, Mod.identifier("soul_star_target"))
    val voidLilyStructureKey: TagKey<Structure> = TagKey.create(net.minecraft.core.registries.Registries.STRUCTURE, Mod.identifier("void_lily_target"))
    
    val voidBlossomCavernPiece: StructurePieceType = createStructurePiece(Mod.identifier("void_blossom_piece"), StructureFactories.voidBlossom)
    val voidBlossomStructureType: StructureType<VoidBlossomArenaStructureFeature> = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, Mod.identifier("void_blossom"), StructureType{VoidBlossomArenaStructureFeature.CODEC})
    val voidBlossomStructureRegistry = StructureRegister(Mod.identifier("void_blossom"))

    private fun createStructurePiece(identifier: Identifier, structurePieceType: StructurePieceType) = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, identifier, structurePieceType)

    private object StructureFactories {
        val voidBlossom: StructurePieceType = StructurePieceType { _, t -> CodeStructurePiece(Mod.structures.voidBlossomCavernPiece, t, VoidBlossomCavernPieceGenerator()) }
    }
}