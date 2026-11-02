package net.barribob.boss.block

import net.minecraft.world.level.block.state.BlockBehaviour
import net.barribob.boss.mob.Entities
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level

class ObsidilithRuneBlock(settings: BlockBehaviour.Properties) : Block(settings) {
    private fun linkToEntities(world: ServerLevel, pos: BlockPos) {
        world.getEntities(Entities.OBSIDILITH, AABB(pos).inflate(15.0, 40.0, 15.0)) { true }.forEach {
            it.addActivePillar(pos)
        }
    }

    override fun tick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        linkToEntities(world, pos)
    }

    override fun onPlace(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        oldState: BlockState,
        notify: Boolean
    ) {
        world.scheduleTick(pos, this, 10)
    }
}