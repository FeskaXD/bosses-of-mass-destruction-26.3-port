package net.barribob.boss.block.structure_repair

import net.barribob.boss.Mod
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.world.level.block.Blocks
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.structure.StructureStart
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.world.level.levelgen.structure.Structure

class LichStructureRepair : StructureRepair {
    override fun associatedStructure(): ResourceKey<Structure> = Mod.structures.lichStructureRegistry.configuredStructureKey

    override fun repairStructure(world: ServerLevel, structureStart: StructureStart) {
        val pos = altarCenter(world, structureStart)

        val altar = ModBlocks.chiseledStoneAltar.defaultBlockState()
        val positions = listOf(pos.west(6), pos.east(6), pos.north(6), pos.south(6))

        for(altarPos in positions) {
            world.setBlock(altarPos, altar, 3)
            world.spawnParticle(Particles.SOUL_FLAME, altarPos.asVec3d().add(0.5, 1.0, 0.5), VecUtils.unit, 10)
        }
    }

    private fun altarCenter(world: ServerLevel, structureStart: StructureStart): BlockPos {
        val boundingBox = structureStart.boundingBox
        val yPos = boundingBox.center.below(16).y
        val centerX = boundingBox.center.x
        val centerZ = boundingBox.center.z
        val gridPos = (-2..2).flatMap { x -> (-2..2).map { z -> Pair(x + centerX, z + centerZ) } }
            .maxByOrNull { xzPair -> countChestsInColumn(boundingBox, world, xzPair)
        }
        return BlockPos(gridPos!!.first, yPos, gridPos.second)
    }

    private fun countChestsInColumn(
        boundingBox: BoundingBox,
        world: ServerLevel,
        xzPair: Pair<Int, Int>,
    ) = (boundingBox.minY()..boundingBox.maxY()).count {
        world.getBlockState(
            BlockPos(
                xzPair.first,
                it,
                xzPair.second
            )
        ).block == Blocks.CHEST
    }

    override fun shouldRepairStructure(world: ServerLevel, structureStart: StructureStart): Boolean {
        val pos = altarCenter(world, structureStart)
        val hasAltar = world.getBlockState(pos.west(6)).block == ModBlocks.chiseledStoneAltar
        val noBoss = world.getEntities(Entities.LICH) { it.distanceToSqr(pos.asVec3d()) < 100 * 100 }.none()
        return !hasAltar && noBoss
    }
}