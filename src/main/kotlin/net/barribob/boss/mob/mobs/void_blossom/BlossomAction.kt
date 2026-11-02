package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.Mod
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.void_blossom.hitbox.HitboxId
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.NetworkUtils.Companion.sendPlacePacket
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.EventSeries
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.world.level.block.Blocks
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.core.BlockPos

class BlossomAction(
    val entity: VoidBlossomEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) : IActionWithCooldown {
    private val blossomPositions = listOf(
        VecUtils.xAxis,
        VecUtils.zAxis,
        VecUtils.xAxis.reverse(),
        VecUtils.zAxis.reverse(),
        VecUtils.xAxis.add(VecUtils.zAxis),
        VecUtils.xAxis.add(VecUtils.zAxis.reverse()),
        VecUtils.xAxis.reverse().add(VecUtils.zAxis),
        VecUtils.xAxis.reverse().add(VecUtils.zAxis.reverse())
    ).map { it.normalize().scale(15.0) }

    override fun perform(): Int {
        val serverWorld = entity.level()
        if (serverWorld !is ServerLevel) return 80

        eventScheduler.addEvent(
            EventSeries(
                TimedEvent({
                    entity.entityData.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.SpikeWave3.id)
                }, 20, shouldCancel = shouldCancel),
                TimedEvent({
                    entity.entityData.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.Idle.id)
                }, 80)
            )
        )

        placeBlossoms(serverWorld)
        return 120
    }

    private fun placeBlossoms(world: ServerLevel) {
        val positions = blossomPositions.map { BlockPos.containing(it.add(entity.position())) }.shuffled()
        val hpRatio = entity.health / entity.maxHealth
        val protectedPositions = if(hpRatio < VoidBlossomEntity.hpMilestones[1]) 6 else if (hpRatio < VoidBlossomEntity.hpMilestones[2]) 3 else 0

        world.playSound(entity.position(), Mod.sounds.spikeWaveIndicator, SoundSource.HOSTILE, 2.0f, 0.7f, 64.0)

        for(i in 0 until 8) {
            eventScheduler.addEvent(TimedEvent({
                val blossomPos = positions[i]
                world.setBlock(blossomPos, Blocks.MOSS_BLOCK.defaultBlockState(), 3)
                world.setBlock(blossomPos.above(), ModBlocks.voidBlossom.defaultBlockState(), 3)
                entity.sendPlacePacket(blossomPos.asVec3d().add(VecUtils.unit.scale(0.5)))
                world.playSound(blossomPos.asVec3d(), Mod.sounds.petalBlade, SoundSource.HOSTILE, 1.0f, entity.random.randomPitch(), 64.0)

                if(i < protectedPositions) {
                    for (x in -1..1) {
                        for (z in -1..1) {
                            for (y in 0..2) {
                                if ((x != 0 || z != 0)) {
                                    world.setBlock(blossomPos.offset(x, y, z), ModBlocks.vineWall.defaultBlockState(), 3)
                                }
                            }
                        }
                    }
                }

            },40 + i * 8, shouldCancel = shouldCancel))
        }
    }
}