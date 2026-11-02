package net.barribob.boss.block.structure_repair

import net.barribob.boss.Mod
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithEffectHandler
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithUtils
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.NetworkUtils
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.levelgen.structure.StructureStart
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.levelgen.structure.Structure

class ObsidilithStructureRepair : StructureRepair {
    override fun associatedStructure(): ResourceKey<Structure> = Mod.structures.obsidilithStructureRegistry.configuredStructureKey

    override fun repairStructure(world: ServerLevel, structureStart: StructureStart) {
        val topCenter = getTopCenter(structureStart)
        val worldEventScheduler = ModComponents.getWorldEventScheduler(world)
        NetworkUtils.sendObsidilithRevivePacket(world, topCenter.asVec3d().add(0.5, 0.5, 0.5))

        for (y in 0..ObsidilithUtils.deathPillarHeight) {
            worldEventScheduler.addEvent(TimedEvent({
                world.playSound(null, topCenter, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, world.random.randomPitch())
                for (pos in ObsidilithUtils.circlePos) {
                    world.destroyBlock(BlockPos(pos.x.toInt(), y, pos.z.toInt()).offset(topCenter), false)
                }
                if(y == 0) {
                    world.setBlock(topCenter, ModBlocks.obsidilithSummonBlock.defaultBlockState(), 3)
                }
            }, y * ObsidilithUtils.ticksBetweenPillarLayer))
        }
    }

    private fun getTopCenter(structureStart: StructureStart): BlockPos {
        val centerPos = structureStart.boundingBox.center
        return BlockPos(centerPos.x, structureStart.boundingBox.maxY(), centerPos.z)
    }

    override fun shouldRepairStructure(world: ServerLevel, structureStart: StructureStart): Boolean {
        val topCenter = getTopCenter(structureStart)
        val noBoss = world.getEntities(Entities.OBSIDILITH) { it.distanceToSqr(topCenter.asVec3d()) < 100 * 100 }.none()
        val hasAltar = world.getBlockState(topCenter).block == ModBlocks.obsidilithSummonBlock
        return noBoss && !hasAltar
    }

    companion object {
        fun handleObsidilithRevivePacket(pos: Vec3, world: ClientLevel) {
            ObsidilithEffectHandler.spawnPillarParticles(pos, ModComponents.getWorldEventScheduler(world))
        }
    }
}