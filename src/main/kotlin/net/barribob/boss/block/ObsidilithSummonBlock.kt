package net.barribob.boss.block

import net.minecraft.core.Direction
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.level.block.state.BlockBehaviour
import net.barribob.boss.block.ModBlocks.obsidilithSummonBlock
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.level.block.Block; import net.minecraft.world.level.block.Blocks; import net.minecraft.world.level.block.EndPortalFrameBlock; import net.minecraft.world.level.block.state.BlockState; import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.InteractionResult
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.math.sin

open class ObsidilithSummonBlock(settings: BlockBehaviour.Properties) : Block(settings) {
    init {
        registerDefaultState(stateDefinition.any().setValue(eye, false))
    }

    override fun useShapeForLightOcclusion(state: BlockState): Boolean {
        return true
    }

    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return if (state.getValue(eye)) frameWithEyeShape else frameShape
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        return defaultBlockState().setValue(eye, false) as BlockState
    }

    override fun hasAnalogOutputSignal(state: BlockState): Boolean {
        return true
    }

    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos, direction: Direction): Int {
        return if (state.getValue(eye) as Boolean) 15 else 0
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(eye)
    }

    companion object {
        val eye: BooleanProperty = BlockStateProperties.EYE
        protected val frameShape: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 13.0, 16.0)
        protected val eyeShape: VoxelShape = box(4.0, 13.0, 4.0, 12.0, 16.0, 12.0)
        protected val frameWithEyeShape: VoxelShape = Shapes.or(frameShape, eyeShape)

        fun onEnderEyeUsed(context: UseOnContext, cir: CallbackInfoReturnable<InteractionResult>) {
            val world = context.level
            val blockPos = context.clickedPos
            val blockState = world.getBlockState(blockPos)
            if (blockState.`is`(obsidilithSummonBlock) && !blockState.getValue(EndPortalFrameBlock.HAS_EYE)) {
                val eventScheduler = ModComponents.getWorldEventScheduler(world)
                if (world.isClientSide) {
                    cir.returnValue = InteractionResult.SUCCESS
                    addSummonEntityEffects(eventScheduler, blockPos)
                } else {
                    val blockState2 = blockState.setValue(EndPortalFrameBlock.HAS_EYE, true)
                    pushEntitiesUp(blockState, blockState2, world, blockPos)
                    world.setBlock(blockPos, blockState2, 2)
                    context.itemInHand.shrink(1)
                    world.levelEvent(1503, blockPos, 0)

                    addSummonEntityEvent(eventScheduler, world, blockPos)
                    cir.returnValue = InteractionResult.PASS
                }
            }
        }

        private val activateParticleFactory = ClientParticleBuilder(Particles.PILLAR_RUNE)
            .scale { (sin(it.toDouble() * Math.PI)).toFloat() * 0.05f }
            .age(30)

        @Environment(EnvType.CLIENT)
        private fun addSummonEntityEffects(
            eventScheduler: EventScheduler,
            blockPos: BlockPos
        ) {
            val centralPos = blockPos.above().asVec3d().add(VecUtils.unit.scale(0.5))
            val particleVel = VecUtils.yAxis.scale(-0.03)
            eventScheduler.addEvent(TimedEvent({
                activateParticleFactory.build(
                    centralPos.add(RandomUtils.randVec().scale(2.0)),
                    particleVel
                )
            }, 0, 80))
        }

        private fun addSummonEntityEvent(
            eventScheduler: EventScheduler,
            world: Level,
            blockPos: BlockPos
        ) {
            val pos = blockPos.asVec3d().add(Vec3(0.5, 0.0, 0.5))
            eventScheduler.addEvent(TimedEvent({
                world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3)
                val obsidilithEntity = Entities.OBSIDILITH.create(world, EntitySpawnReason.TRIGGERED)
                if (obsidilithEntity != null) {
                    obsidilithEntity.syncPacketPositionCodec(pos.x, pos.y, pos.z)
                    obsidilithEntity.absSnapTo(pos.x, pos.y, pos.z)
                    world.addFreshEntity(obsidilithEntity)
                }
            }, 100))
        }
    }
}