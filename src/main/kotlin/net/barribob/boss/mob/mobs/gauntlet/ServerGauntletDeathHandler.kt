package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.Entity
import net.minecraft.world.RandomizableContainer
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class ServerGauntletDeathHandler(
    val entity: GauntletEntity,
    private val eventScheduler: EventScheduler,
    private val mobConfig: GauntletConfig
) : IEntityTick<ServerLevel> {
    override fun tick(world: ServerLevel) {
        ++entity.deathTime
        if (entity.deathTime == deathAnimationTime) {
            world.explode(null, entity.position().x, entity.position().y, entity.position().z, 4.0f, Level.ExplosionInteraction.MOB)
            if (mobConfig.spawnAncientDebrisOnDeath) createLoot(world)
            dropExp()
            entity.remove(Entity.RemovalReason.KILLED)
        }
    }

    private fun createLoot(world: ServerLevel) {
        for (i in 0..4) {
            val randomDir = RandomUtils.randVec().normalize()
            val length = 8 - i
            val start = entity.position()
            val end = entity.position().add(randomDir.scale(length.toDouble()))
            val points = length * 2
            MathUtils.lineCallback(start, end, points) { vec3d: Vec3, point: Int ->
                val blockPos = BlockPos.containing(vec3d)
                if (point == points - 1) world.setBlock(blockPos, Blocks.ANCIENT_DEBRIS.defaultBlockState(), 3)
                else world.setBlock(blockPos, Blocks.NETHERRACK.defaultBlockState(), 3)
            }
        }
        val chestPos = entity.blockPosition().above()
        world.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2)
        RandomizableContainer.setBlockEntityLootTable(world, entity.random, chestPos, Mod.loot.gauntlet)
    }

    private fun dropExp() {
        val expTicks = 20
        val expPerTick = (mobConfig.experienceDrop / expTicks.toFloat()).toInt()
        val pos = entity.position()
        eventScheduler.addEvent(TimedEvent({
            VanillaCopies.awardExperience(
                expPerTick,
                pos.add(RandomUtils.randVec().planeProject(VecUtils.yAxis).scale(2.0)),
                entity.level()
            )
        }, 0, expTicks))
    }

    companion object {
        const val deathAnimationTime = 50
    }
}