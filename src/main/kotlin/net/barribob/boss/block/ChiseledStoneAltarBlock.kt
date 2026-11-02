package net.barribob.boss.block

import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockBehaviour
import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.ParticleFactories
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level

open class ChiseledStoneAltarBlock(settings: BlockBehaviour.Properties) : Block(settings) {
    init {
        registerDefaultState(stateDefinition.any().setValue(lit, false))
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext) = defaultBlockState().setValue(lit, false) as BlockState

    override fun hasAnalogOutputSignal(state: BlockState) = true
    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos, direction: Direction): Int {
        return if (state.getValue(lit) as Boolean) 15 else 0
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(lit)
    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        if (state.getValue(lit)) {
            if(random.nextInt(3) == 0) {
                blueFireParticleFactory.build(
                    pos.asVec3d().add(0.5, 1.0, 0.5)
                        .add(RandomUtils.randVec().planeProject(VecUtils.yAxis).scale(0.5)),
                    VecUtils.yAxis.scale(0.05)
                )
            }
        } else {
            paleSparkleParticleFactory.build(
                pos.asVec3d().add(0.5, 2.0, 0.5).add(RandomUtils.randVec().scale(0.5)),
                VecUtils.yAxis.scale(-0.05)
            )
        }
    }

    companion object {
        val lit: BooleanProperty = BlockStateProperties.LIT

        private val paleSparkleParticleFactory = ClientParticleBuilder(Particles.DOWNSPARKLE)
            .color { MathUtils.lerpVec(it, ModColors.WHITE, ModColors.GREY) }
            .age(20, 30)
            .colorVariation(0.1)
            .scale { 0.15f - (it * 0.1f) }
        val blueFireParticleFactory = ParticleFactories.soulFlame()
            .color(LichUtils.blueColorFade)
            .age(30, 40)
            .colorVariation(0.5)
            .scale { 0.15f - (it * 0.1f) }

    }
}