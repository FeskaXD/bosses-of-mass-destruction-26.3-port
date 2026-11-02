package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerLevel

class VoidBlossomDropExpDeathTick(
    private val entity: LivingEntity,
    private val eventScheduler: EventScheduler,
    private val exp: Int,
) : IEntityTick<ServerLevel> {
    override fun tick(world: ServerLevel) {
        if(entity.deathTime == 1) {
            scheduleExp()
        }
    }

    private fun scheduleExp() {
        val expTicks = 20
        val expPerTick = (exp / expTicks.toFloat()).toInt()
        val fallDirection = entity.forward.planeProject(VecUtils.yAxis).yRot(180f)
        val originPos = entity.position().add(VecUtils.yAxis.scale(2.0))

        eventScheduler.addEvent(TimedEvent({
            val pos = originPos
                .add(RandomUtils.randVec().scale(2.0))
                .add(fallDirection.scale(RandomUtils.double(6.0) + 6.0))

            VanillaCopies.awardExperience(
                expPerTick,
                pos,
                entity.level()
            )
        }, (LightBlockRemover.deathMaxAge - expTicks - 1).toInt(), expTicks))
    }
}