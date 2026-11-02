package net.barribob.boss.block

import net.minecraft.util.RandomSource
import net.minecraft.world.level.ScheduledTickAccess
import net.minecraft.world.level.block.state.BlockBehaviour
import com.mojang.serialization.MapCodec
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.utils.VanillaCopies
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.world.level.block.BaseEntityBlock; import net.minecraft.world.level.block.Block; import net.minecraft.world.level.block.Blocks; import net.minecraft.world.level.block.DoorBlock; import net.minecraft.world.level.block.EntityBlock; import net.minecraft.world.level.block.HorizontalDirectionalBlock; import net.minecraft.world.level.block.RenderShape; import net.minecraft.world.level.block.state.BlockState; import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import kotlin.math.abs

class MonolithBlock(private val factory: (FabricBlockEntityTypeBuilder.Factory<ChunkCacheBlockEntity>)?, settings: BlockBehaviour.Properties) : BaseEntityBlock(settings),
    EntityBlock, BlockTooltip {

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
            .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER))
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        display: net.minecraft.world.item.component.TooltipDisplay,
        tooltip: java.util.function.Consumer<Component>,
        options: TooltipFlag
    ) {
        tooltip.accept(Component.translatable("item.bosses_of_mass_destruction.monolith_block.tooltip_0").withStyle(ChatFormatting.DARK_GRAY))
        tooltip.accept(Component.translatable("item.bosses_of_mass_destruction.monolith_block.tooltip_1").withStyle(ChatFormatting.DARK_GRAY))
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = factory?.create(pos, state)
    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun <T : BlockEntity> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(type, ModBlocks.monolithEntityType!!, ChunkCacheBlockEntity::tick)
    }

    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return if (state.getValue(HorizontalDirectionalBlock.FACING).axis === Direction.Axis.X) xAxisShape else zAxisShape
    }

    override fun updateShape(
        state: BlockState,
        world: LevelReader,
        scheduledTickAccess: ScheduledTickAccess,
        pos: BlockPos,
        direction: Direction,
        posFrom: BlockPos,
        newState: BlockState,
        random: RandomSource
    ): BlockState {
        val doubleBlockHalf = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)
        val airState = Blocks.AIR.defaultBlockState()
        return if (direction.axis === Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            if (newState.`is`(this) && newState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != doubleBlockHalf) {
                state.setValue(HorizontalDirectionalBlock.FACING, newState.getValue(HorizontalDirectionalBlock.FACING))
            } else {
                airState
            }
        } else {
            super.updateShape(state, world, scheduledTickAccess, pos, direction, posFrom, newState, random)
        }
    }

    override fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!world.isClientSide && player.isCreative) {
            VanillaCopies.onBreakInCreative(world, pos, state, player)
        }
        return super.playerWillDestroy(world, pos, state, player)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        val blockPos = ctx.clickedPos
        return if (blockPos.y < 255 && ctx.level.getBlockState(blockPos.above()).canBeReplaced(ctx)) {
            defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, ctx.horizontalDirection)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
        } else {
            null
        }
    }

    override fun setPlacedBy(
        world: Level,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack
    ) {
        world.setBlock(pos.above(), state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER) as BlockState, 3)
    }

    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        val blockPos = pos.below()
        val blockState = world.getBlockState(blockPos)
        return if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) blockState.isFaceSturdy(
            world,
            blockPos,
            Direction.UP
        ) else blockState.`is`(this)
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(
            HorizontalDirectionalBlock.FACING,
            rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING) as Direction)
        )
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return if (mirror == Mirror.NONE) state else state.rotate(
            mirror.getRotation(
                state.getValue(
                    HorizontalDirectionalBlock.FACING
                )
            )
        ).cycle(DoorBlock.HINGE)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(BlockStateProperties.DOUBLE_BLOCK_HALF, HorizontalDirectionalBlock.FACING)
    }

    companion object {
        private val xAxisShape = box(3.5, 0.0, 1.5, 12.5, 16.0, 14.5)
        private val zAxisShape = box(1.5, 0.0, 3.5, 14.5, 16.0, 12.5)

        fun getExplosionPower(
            serverWorld: Level,
            pos: BlockPos,
            power: Float
        ): Float {
            val chunkPos = ChunkPos.containing(pos)
            val blockCache = ModComponents.getChunkBlockCache(serverWorld)
            if (blockCache.isPresent) {
                for (x in chunkPos.x - 4..chunkPos.x + 4) {
                    for (z in chunkPos.z - 4..chunkPos.z + 4) {
                        val blocks = blockCache.get().getBlocksFromChunk(ChunkPos(x, z), ModBlocks.monolithBlock)
                        if (blocks.any { abs(it.x - pos.x) < 64 && abs(it.y - pos.y) < 64 && abs(it.z - pos.z) < 64 }) {
                            return power * 1.3f
                        }
                    }
                }
            }

            return power
        }
    }
}