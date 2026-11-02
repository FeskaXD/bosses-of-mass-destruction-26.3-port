package net.barribob.boss.block

import net.minecraft.world.level.ScheduledTickAccess
import net.minecraft.world.level.block.state.BlockBehaviour
import com.mojang.serialization.MapCodec
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.world.level.block.BaseEntityBlock; import net.minecraft.world.level.block.Block; import net.minecraft.world.level.block.Blocks; import net.minecraft.world.level.block.DoorBlock; import net.minecraft.world.level.block.EntityBlock; import net.minecraft.world.level.block.HorizontalDirectionalBlock; import net.minecraft.world.level.block.RenderShape; import net.minecraft.world.level.block.state.BlockState; import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.util.RandomSource
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class MobWardBlock(private val factory: (FabricBlockEntityTypeBuilder.Factory<ChunkCacheBlockEntity>)?, settings: BlockBehaviour.Properties) :
    BaseEntityBlock(settings),
    EntityBlock, BlockTooltip {
    init {
        registerDefaultState(stateDefinition.any()
            .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
            .setValue(tripleBlockPart, TripleBlockPart.BOTTOM))
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL
    override fun newBlockEntity(pos: BlockPos, state: BlockState): ChunkCacheBlockEntity? = factory?.create(pos, state)
    override fun <T : BlockEntity> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(type, ModBlocks.mobWardEntityType!!, ChunkCacheBlockEntity::tick)
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        display: net.minecraft.world.item.component.TooltipDisplay,
        tooltip: java.util.function.Consumer<Component>,
        options: TooltipFlag
    ) {
        tooltip.accept(Component.translatable("item.bosses_of_mass_destruction.mob_ward.tooltip").withStyle(ChatFormatting.DARK_GRAY))
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
        val thisState = state.getValue(tripleBlockPart)
        val superState = super.updateShape(state, world, scheduledTickAccess, pos, direction, posFrom, newState, random)
        val air = Blocks.AIR.defaultBlockState()
        val otherState = newState.`is`(this)
        val facingState = if (otherState) state.setValue(
            HorizontalDirectionalBlock.FACING,
            newState.getValue(HorizontalDirectionalBlock.FACING)
        ) else air

        return when (thisState) {
            TripleBlockPart.BOTTOM -> when (direction) {
                Direction.UP -> if (otherState && newState.getValue(tripleBlockPart) == TripleBlockPart.MIDDLE) facingState else air
                else -> superState
            }
            TripleBlockPart.MIDDLE -> when (direction) {
                Direction.UP -> if (otherState && newState.getValue(tripleBlockPart) == TripleBlockPart.TOP) facingState else air
                Direction.DOWN -> if (otherState && newState.getValue(tripleBlockPart) == TripleBlockPart.BOTTOM) facingState else air
                else -> superState
            }
            TripleBlockPart.TOP -> when (direction) {
                Direction.DOWN -> if (otherState && newState.getValue(tripleBlockPart) == TripleBlockPart.MIDDLE) facingState else air
                else -> superState
            }
            else -> air
        }
    }

    override fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!world.isClientSide && player.isCreative) {
            val part = state.getValue(tripleBlockPart)
            if (part == TripleBlockPart.MIDDLE) {
                checkBreakPart(pos.below(), world, state, player, TripleBlockPart.BOTTOM)
                checkBreakPart(pos.above(), world, state, player, TripleBlockPart.TOP)
            } else if (part == TripleBlockPart.TOP) {
                checkBreakPart(pos.below(2), world, state, player, TripleBlockPart.BOTTOM)
                checkBreakPart(pos.below(), world, state, player, TripleBlockPart.MIDDLE)
            }
        }

        return super.playerWillDestroy(world, pos, state, player)
    }

    private fun checkBreakPart(
        pos: BlockPos,
        world: Level,
        state: BlockState,
        player: Player,
        part: TripleBlockPart
    ) {
        val blockState2 = world.getBlockState(pos)
        if (blockState2.block === state.block && blockState2.getValue(tripleBlockPart) == part) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 35)
            world.levelEvent(player, 2001, pos, getId(blockState2))
        }
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        val blockPos = ctx.clickedPos
        return if (blockPos.y < ctx.level.height - 2 &&
            ctx.level.getBlockState(blockPos.above()).canBeReplaced(ctx) &&
            ctx.level.getBlockState(blockPos.above(2)).canBeReplaced(ctx)
        ) {
            ctx.level
            defaultBlockState().setValue(DoorBlock.FACING, ctx.horizontalDirection).setValue(tripleBlockPart, TripleBlockPart.BOTTOM)
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
        world.setBlock(pos.above(), state.setValue(tripleBlockPart, TripleBlockPart.MIDDLE) as BlockState, 3)
        world.setBlock(pos.above(2), state.setValue(tripleBlockPart, TripleBlockPart.TOP) as BlockState, 3)
    }

    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        val blockPos = pos.below()
        val blockState = world.getBlockState(blockPos)
        return if (state.getValue(tripleBlockPart) == TripleBlockPart.BOTTOM) blockState.isFaceSturdy(
            world,
            blockPos,
            Direction.UP
        ) else blockState.`is`(this)
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(
            HorizontalDirectionalBlock.FACING, rotation.rotate(
                state.getValue(HorizontalDirectionalBlock.FACING)
            )
        )
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return if (mirror == Mirror.NONE) state else state.rotate(mirror.getRotation(state.getValue(DoorBlock.FACING)))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder!!.add(HorizontalDirectionalBlock.FACING, tripleBlockPart)
    }

    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return if (state.getValue(tripleBlockPart) == TripleBlockPart.TOP) blockShape else thinBlockShape
    }

    private val blueFireParticleFactory = ClientParticleBuilder(Particles.SOUL_FLAME)
        .color(LichUtils.blueColorFade)
        .age(30, 40)
        .colorVariation(0.5)
        .scale { 0.15f - (it * 0.1f) }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        if (state.getValue(tripleBlockPart) == TripleBlockPart.TOP) {
            if (random.nextInt(3) == 0) {
                val vecPos = pos.asVec3d().add(VecUtils.unit.scale(0.5))
                val randomHeight = RandomUtils.double(0.25) + 0.25
                val randomRadius = RandomUtils.double(0.1) + 0.3
                val randomOffset = RandomUtils.double(Math.PI)
                blueFireParticleFactory.continuousPosition {
                    calcParticlePos(
                        vecPos,
                        randomOffset,
                        randomRadius,
                        randomHeight,
                        it.getAge().toDouble() * 0.1
                    )
                }.build(
                    calcParticlePos(
                        vecPos,
                        randomOffset,
                        randomRadius,
                        randomHeight,
                        .0
                    )
                )
            }
        }
    }

    private fun calcParticlePos(
        vecPos: Vec3,
        randomOffset: Double,
        randomRadius: Double,
        randomHeight: Double,
        age: Double
    ): Vec3 = vecPos.add(
        Vec3(
            sin(age + randomOffset) * randomRadius,
            randomHeight + age * 0.3,
            cos(age + randomOffset) * randomRadius
        )
    )

    companion object {
        val blockShape: VoxelShape = box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0)
        val thinBlockShape: VoxelShape = box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0)
        val tripleBlockPart: EnumProperty<TripleBlockPart> = EnumProperty.create("triple_part", TripleBlockPart::class.java)

        fun canSpawn(serverWorld: ServerLevel, pos: BlockPos.MutableBlockPos, cir: CallbackInfoReturnable<Boolean>) {
            if (cir.returnValue == false) return

            val chunkPos = ChunkPos.containing(pos)
            ModComponents.getChunkBlockCache(serverWorld).ifPresent { component ->
                for (x in chunkPos.x - 4..chunkPos.x + 4) {
                    for (z in chunkPos.z - 4..chunkPos.z + 4) {
                        val blocks = component.getBlocksFromChunk(ChunkPos(x, z), ModBlocks.mobWard)
                        if (blocks.any { abs(it.x - pos.x) < 64 && abs(it.y - pos.y) < 64 && abs(it.z - pos.z) < 64 }) {
                            cir.returnValue = false
                        }
                    }
                }
            }
        }
    }
}