package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

class VoidBlossomClientSpikeHandler: IEntityTick<ClientLevel> {
    private val maxAge = 10
    private val spikes = mutableMapOf<BlockPos, Spike>()
    private val spikeParticleFactory = ClientParticleBuilder(Particles.SPARKLES)
        .age { RandomUtils.range(10, 15) }
        .color(ModColors.VOID_PURPLE)
        .colorVariation(0.25)
        .brightness(Particles.FULL_BRIGHT)

    fun getSpikes() = spikes.toMap()

    fun addSpike(pos: BlockPos) {
        val center = pos.asVec3d().add(VecUtils.unit.scale(0.5))
        val spikeHeight = 4.0 + RandomUtils.double(0.5)
        spikes[pos] = Spike(
            center,
            RandomUtils.randVec().add(VecUtils.yAxis.scale(spikeHeight)).normalize(),
            (spikeHeight).toFloat(),
            maxAge
        )
    }

    override fun tick(world: ClientLevel) {
        val toRemove = mutableListOf<BlockPos>()

        for(kv in spikes) {
            val age = kv.value.age++

            if (age == maxAge - 5) {
                spikeParticleFactory.build(
                    kv.key.asVec3d()
                        .add(RandomUtils.randVec().add(VecUtils.yAxis.scale(2.5 + RandomUtils.double(2.0))))
                )
            }

            if(age >= maxAge) {
                toRemove.add(kv.key)
            }
        }

        for(removal in toRemove) {
            spikes.remove(removal)
        }
    }

    data class Spike(val pos: Vec3, val offset: Vec3, val height: Float, val maxAge: Int, var age: Int = 0)
}