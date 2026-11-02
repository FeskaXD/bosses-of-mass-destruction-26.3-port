package net.barribob.boss.cardinalComponents

import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.block.Block
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.HolderLookup
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level

class ChunkBlockCacheComponent(val world: Level) : IChunkBlockCacheComponent {
    private val map = hashMapOf<ChunkPos, HashMap<Block, MutableSet<BlockPos>>>()

    override fun addToChunk(chunkPos: ChunkPos, block: Block, pos: BlockPos) {
        val chunk = map[chunkPos] ?: hashMapOf()
        val blocks = chunk[block] ?: mutableSetOf()
        blocks.add(pos)
        chunk[block] = blocks
        map[chunkPos] = chunk
    }

    override fun getBlocksFromChunk(chunkPos: ChunkPos, block: Block): List<BlockPos> = map[chunkPos]?.get(block)?.toList() ?: listOf()

    override fun removeFromChunk(chunkPos: ChunkPos, block: Block, pos: BlockPos) {
        map[chunkPos]?.get(block)?.remove(pos)
    }

    override fun readData(input: ValueInput) {
    }

    override fun writeData(output: ValueOutput) {
    }
}