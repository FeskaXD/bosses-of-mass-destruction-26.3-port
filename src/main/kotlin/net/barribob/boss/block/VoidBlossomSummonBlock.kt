package net.barribob.boss.block

import net.minecraft.world.level.block.state.BlockBehaviour
import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import java.lang.UnsupportedOperationException

class VoidBlossomSummonBlock(private val factory: (FabricBlockEntityTypeBuilder.Factory<VoidBlossomSummonBlockEntity>)?, settings: BlockBehaviour.Properties) : BaseEntityBlock(settings) {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = factory?.create(pos, state)
    override fun <T : BlockEntity> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(type, ModBlocks.voidBlossomSummonBlockEntityType!!, VoidBlossomSummonBlockEntity::tick)
    }
}