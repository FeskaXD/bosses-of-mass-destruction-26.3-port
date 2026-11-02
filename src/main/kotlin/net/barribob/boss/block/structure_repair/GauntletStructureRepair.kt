package net.barribob.boss.block.structure_repair

import net.barribob.boss.Mod
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.structure.StructureStart
import net.minecraft.core.BlockPos
import net.minecraft.world.level.levelgen.structure.Structure

class GauntletStructureRepair : StructureRepair {
    override fun associatedStructure(): ResourceKey<Structure> = Mod.structures.gauntletStructureRegistry.configuredStructureKey
    override fun repairStructure(world: ServerLevel, structureStart: StructureStart) {
        val pos = runeCenter(structureStart)

        world.spawnParticle(Particles.GAUNTLET_REVIVE_SPARKLES, pos.above(5).asVec3d(), VecUtils.unit.scale(3.0), 100)

        spawnBlocks(world, pos)
    }

    private fun spawnBlocks(world: ServerLevel, pos: BlockPos) {
        for (x in -1..1) {
            for (z in -1..1) {
                for (y in 0..4) {
                    world.setBlock(pos.offset(x, y, z), ModBlocks.sealedBlackstone.defaultBlockState(), 3)
                }
            }
        }
        val up = pos.above()
        val seal = ModBlocks.gauntletBlackstone.defaultBlockState()
        world.setBlock(up, seal, 3)
        world.setBlock(up.east(), seal, 3)
        world.setBlock(up.north(), seal, 3)
        world.setBlock(up.west(), seal, 3)
        world.setBlock(up.south(), seal, 3)
    }

    private fun runeCenter(structureStart: StructureStart): BlockPos {
        return structureStart.boundingBox.center.below(10)
    }

    override fun shouldRepairStructure(world: ServerLevel, structureStart: StructureStart): Boolean {
        val pos = runeCenter(structureStart)
        val hasSealAlready = world.getBlockState(pos.above()).block == ModBlocks.gauntletBlackstone
        val noBoss = world.getEntities(Entities.GAUNTLET) { it.distanceToSqr(pos.asVec3d()) < 100 * 100 }.none()
        return !hasSealAlready && noBoss
    }
}