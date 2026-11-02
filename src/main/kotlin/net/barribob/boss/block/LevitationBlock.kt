package net.barribob.boss.block

import net.minecraft.world.level.block.state.BlockBehaviour
import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.world.level.block.BaseEntityBlock; import net.minecraft.world.level.block.EntityBlock; import net.minecraft.world.level.block.RenderShape; import net.minecraft.world.level.block.state.BlockState; import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level

class LevitationBlock(private val factory: (FabricBlockEntityTypeBuilder.Factory<LevitationBlockEntity>)?, settings: BlockBehaviour.Properties) : BaseEntityBlock(settings), EntityBlock, BlockTooltip {
    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        display: net.minecraft.world.item.component.TooltipDisplay,
        tooltip: java.util.function.Consumer<Component>,
        options: TooltipFlag
    ) {
        tooltip.accept(Component.translatable("item.bosses_of_mass_destruction.levitation_block.tooltip").withStyle(ChatFormatting.DARK_GRAY))
    }

    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape = Shapes.or(bottomShape, tableShape)

    private val bottomShape = box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0)
    private val tableShape = box(2.0, 2.0, 2.0, 14.0, 14.0, 14.0)
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = factory?.create(pos, state)

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun <T : BlockEntity> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(type, ModBlocks.levitationBlockEntityType!!, LevitationBlockEntity::tick)
    }
}