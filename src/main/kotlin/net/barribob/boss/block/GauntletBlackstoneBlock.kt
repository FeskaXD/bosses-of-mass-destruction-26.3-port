package net.barribob.boss.block

import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.level.block.state.BlockBehaviour
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.player.Player
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.core.Vec3i
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level

class GauntletBlackstoneBlock(settings: BlockBehaviour.Properties) : Block(settings) {
    val laserChargeParticles = ClientParticleBuilder(Particles.SPARKLES)
        .brightness(Particles.FULL_BRIGHT)
        .color(ModColors.LASER_RED)
        .colorVariation(0.2)

    override fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {

        if(world.isClientSide) return state

        for (dir in listOf(Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH)) {
            val centerPos = pos.offset(dir.unitVec3i)
            val centerBlock = world.getBlockState(centerPos).block
            if (centerBlock == this) {
                spawnGauntlet(centerPos, world)
                break
            }
        }
        return super.playerWillDestroy(world, pos, state, player)
    }

    private fun spawnGauntlet(centerPos: BlockPos, world: Level) {
        val spawnPos = centerPos.asVec3d().add(Vec3(0.5, -0.5, 0.5))
        val entity = Entities.GAUNTLET.create(world, EntitySpawnReason.TRIGGERED)
        entity!!.absSnapTo(spawnPos.x, spawnPos.y, spawnPos.z)
        world.addFreshEntity(entity)

        val eventScheduler = ModComponents.getWorldEventScheduler(world)

        for (y in -1..4) {
            eventScheduler.addEvent(TimedEvent({
                for (x in -1..1) {
                    for (z in -1..1) {
                        world.destroyBlock(centerPos.offset(Vec3i(x, y, z)), false)
                    }
                }
            }, 10 + y * 5))
        }
    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        val particlePos = pos.asVec3d().add(VecUtils.unit.scale(0.5)).add(RandomUtils.randVec().normalize())
        laserChargeParticles.build(particlePos)
    }
}