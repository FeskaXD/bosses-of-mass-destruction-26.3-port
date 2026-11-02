package net.barribob.boss.block

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.NetworkUtils.Companion.sendParticlePacket
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.HolderLookup
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import kotlin.math.sin

class VoidLilyBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    BlockEntity(type, pos, state) {
    private var structureDirection: Vec3? = null

    override fun loadAdditional(input: net.minecraft.world.level.storage.ValueInput) {
        super.loadAdditional(input)
        val x = input.getDoubleOr("dirX", Double.NaN)
        if (!x.isNaN()) {
            structureDirection = Vec3(x, input.getDoubleOr("dirY", 0.0), input.getDoubleOr("dirZ", 0.0))
        }
    }

    override fun saveAdditional(output: net.minecraft.world.level.storage.ValueOutput) {
        super.saveAdditional(output)
        val dir = structureDirection
        if (dir != null) {
            output.putDouble("dirX", dir.x)
            output.putDouble("dirY", dir.y)
            output.putDouble("dirZ", dir.z)
        }
    }

    companion object {
        fun tick(world: Level, pos: BlockPos, state: BlockState, entity: VoidLilyBlockEntity) {
            if (RandomUtils.range(0, 30) == 0 && world is ServerLevel) {
                val direction = entity.structureDirection
                if (direction == null) {
                    setNearestStructureDirection(world, pos, entity)
                }

                if (direction != null) {
                    sendParticlePacket(world, pos, direction)
                }
            }
        }

        private fun setNearestStructureDirection(
            world: ServerLevel,
            pos: BlockPos,
            entity: VoidLilyBlockEntity
        ) {
            val blockPos = world.findNearestMapStructure(Mod.structures.voidLilyStructureKey, pos, 50, false)
            if (blockPos != null) {
                entity.structureDirection = BlockPos(blockPos.x, world.minY, blockPos.z).subtract(pos).asVec3d().normalize()
            } else {
                entity.structureDirection = VecUtils.yAxis
            }
        }

        private val pollenParticles = ClientParticleBuilder(Particles.POLLEN)
            .scale { age -> sin(age * Math.PI.toFloat()) * 0.04f }
            .age(30)

        fun spawnVoidLilyParticles(world: Level, pos: Vec3, dir: Vec3) {
            val streakPos = pos.add(Vec3(0.5, 0.7, 0.5)).add(RandomUtils.randVec().scale(0.5))
            val right = dir.cross(VecUtils.yAxis).normalize()
            val sinCurve = RandomUtils.range(8.0, 11.0) * RandomUtils.randSign()
            val curveLength = RandomUtils.range(1.2, 1.6)
            val particleAmount = RandomUtils.range(7, 10)

            ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
                val particlePos = streakPos.add(RandomUtils.randVec().scale(0.05))
                pollenParticles
                    .continuousPosition { p ->
                        val age = p.ageRatio
                        val forward = dir.scale(age.toDouble() * curveLength)
                        val sinSide = right.scale(sin(age * sinCurve) * 0.1)
                        particlePos.add(forward).add(sinSide)
                    }
                    .build(particlePos)
            }, 0, particleAmount))
        }
    }
}