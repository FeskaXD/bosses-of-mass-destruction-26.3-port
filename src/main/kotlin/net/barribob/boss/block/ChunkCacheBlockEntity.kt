package net.barribob.boss.block

import net.barribob.boss.cardinalComponents.ModComponents
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level

open class ChunkCacheBlockEntity(
    private val block: Block, type: BlockEntityType<*>,
    pos: BlockPos, state: BlockState
) : BlockEntity(type, pos, state) {
    private var added = false

    companion object {
        fun tick(world: Level, pos: BlockPos, state: BlockState, entity: ChunkCacheBlockEntity) {
            if (!entity.added) {
                ModComponents.getChunkBlockCache(world).ifPresent {
                    it.addToChunk(ChunkPos.containing(pos), entity.block, pos)
                    entity.added = true
                }
            }
        }
    }

    override fun setRemoved() {
        val world = level
        if (world != null) {
            ModComponents.getChunkBlockCache(world).ifPresent {
                it.removeFromChunk(ChunkPos.containing(blockPos), block, blockPos)
            }
            added = false
        }
        super.setRemoved()
    }
}