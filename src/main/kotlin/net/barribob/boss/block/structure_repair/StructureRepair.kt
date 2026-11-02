package net.barribob.boss.block.structure_repair

import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.structure.StructureStart
import net.minecraft.world.level.levelgen.structure.Structure

interface StructureRepair {
    fun associatedStructure(): ResourceKey<Structure>
    fun repairStructure(world: ServerLevel, structureStart: StructureStart)
    fun shouldRepairStructure(world: ServerLevel, structureStart: StructureStart): Boolean
}