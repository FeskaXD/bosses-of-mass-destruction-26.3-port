package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.findGroundBelow
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

class PillarAction(val entity: LivingEntity) : IActionWithCooldown {
    private val eventScheduler = ModComponents.getWorldEventScheduler(entity.level())

    override fun perform(): Int {
        val serverWorld = entity.level()
        if(serverWorld !is ServerLevel) return 80
        val pillarPositions = getPillarPositions()
        serverWorld.playSound(entity.position(), Mod.sounds.obsidilithPrepareAttack, SoundSource.HOSTILE, 3.0f, 1.4f, 64.0)

        pillarPositions.forEach {
            MathUtils.lineCallback(entity.eyePos(), it.asVec3d().add(0.5, 0.5, 0.5), pillarXzDistance.toInt()) { pos, _ ->
                serverWorld.spawnParticle(Particles.PILLAR_SPAWN_INDICATOR_2, pos, Vec3.ZERO)
            }
            serverWorld.spawnParticle(Particles.PILLAR_SPAWN_INDICATOR, it.above(5).asVec3d(), Vec3(0.3, 3.0, 0.3), 20)
        }

        eventScheduler.addEvent(TimedEvent({
            pillarPositions.forEach {buildPillar(it, serverWorld)}
        }, pillarDelay, shouldCancel = { !entity.isAlive }))
        return 100
    }

    private fun getPillarPositions(): List<BlockPos> {
        val numPillars = 4
        val pillars = mutableListOf<BlockPos>()

        for (i in 0 until numPillars) {
            val position = RandomUtils.randVec().planeProject(VecUtils.yAxis).normalize().scale(pillarXzDistance).add(entity.position())
            val up = entity.level().findGroundBelow(BlockPos.containing(position).above(14))
            val ground = entity.level().findGroundBelow(up)

            if (up.y - ground.y > maxYDistance) continue

            pillars.add(ground)
        }

        return pillars
    }

    private fun buildPillar(pos: BlockPos, serverWorld: ServerLevel) {
        val pillarHeight = 2
        for (i in 0 until pillarHeight) {
            entity.level().setBlock(pos.above(i), Blocks.OBSIDIAN.defaultBlockState(), 3)
        }
        val pillarTop = pos.above(pillarHeight)
        entity.level().setBlock(pillarTop, ModBlocks.obsidilithRune.defaultBlockState(), 3)
        serverWorld.playSound(pos.asVec3d(), SoundEvents.BASALT_PLACE, SoundSource.HOSTILE, 1.0f)
    }

    companion object {
        const val pillarXzDistance = 13.0
        const val maxYDistance = 15.0
        const val pillarDelay = 40
    }
}