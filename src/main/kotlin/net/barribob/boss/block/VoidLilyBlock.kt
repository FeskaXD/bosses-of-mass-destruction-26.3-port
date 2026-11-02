package net.barribob.boss.block

import net.minecraft.world.level.block.state.BlockBehaviour
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.FlowerBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

class VoidLilyBlock(
    private val factory: (FabricBlockEntityTypeBuilder.Factory<VoidLilyBlockEntity>)?,
    settings: BlockBehaviour.Properties
) : FlowerBlock(MobEffects.GLOWING, 0f, settings), EntityBlock, BlockTooltip {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = factory?.create(pos, state)
    override fun <T : BlockEntity> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> = BlockEntityTicker { tickerWorld, pos, tickerState, blockEntity ->
        if (blockEntity is VoidLilyBlockEntity) VoidLilyBlockEntity.tick(
            tickerWorld,
            pos,
            tickerState,
            blockEntity
        )
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        display: net.minecraft.world.item.component.TooltipDisplay,
        tooltip: java.util.function.Consumer<Component>,
        options: TooltipFlag
    ) {
        tooltip.accept(Component.translatable("block.bosses_of_mass_destruction.void_lily.tooltip").withStyle(ChatFormatting.DARK_GRAY))
    }
}