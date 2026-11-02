package net.barribob.boss.utils

import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.levelgen.structure.Structure

class StructureRegister(structureIdentifier: Identifier) {
    val configuredStructureKey: ResourceKey<Structure> = createConfigureStructureKey(structureIdentifier)
    private fun createConfigureStructureKey(identifier: Identifier) = ResourceKey.create(Registries.STRUCTURE,  identifier)
}