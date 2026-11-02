package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.utils.ModUtils.findGroundBelow
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.context.DirectionalPlaceContext
import net.minecraft.world.item.ItemStack
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

class RiftBurst(
    val entity: LivingEntity,
    val world: ServerLevel,
    private val indicatorParticle: ParticleOptions,
    private val columnParticle: ParticleOptions,
    private val riftTime: Int,
    val eventScheduler: EventScheduler,
    val onImpact: (LivingEntity) -> Unit,
    val isOpenBlock: (BlockPos) -> Boolean = isOpenBlock(world),
    val posFinder: (Vec3) -> BlockPos? = defaultPosFinder(world, isOpenBlock)
) {
    fun tryPlaceRift(pos: Vec3){
        val placement = posFinder(pos)
        if (placement != null) {
            placeRift(placement)
        }
    }

    private fun placeRift(pos: BlockPos) {
        world.spawnParticle(
            indicatorParticle,
            pos.asVec3d().add(Vec3(0.5, 0.1, 0.5)),
            Vec3.ZERO
        )

        eventScheduler.addEvent(TimedEvent({
            val rand = world.random
            val columnVel = VecUtils.yAxis.scale(rand.nextDouble() + 1).scale(0.25)
            var ticks = 0
            eventScheduler.addEvent(TimedEvent({
                val impactPos = pos.above(ticks)
                world.spawnParticle(
                    columnParticle,
                    impactPos.asVec3d().add(VecUtils.unit.scale(0.5)).add(RandomUtils.randVec().scale(0.25)),
                    columnVel
                )

                val box = AABB(impactPos)
                val entities = world.getEntitiesOfClass(LivingEntity::class.java, box) { it != entity }

                entities.forEach {
                    if (it != entity) {
                        onImpact(it)
                    }
                }

                ticks += 2
            }, 0, 7))
        }, riftTime, shouldCancel = { !isOpenBlock(pos) || !entity.isAlive }))
    }
}

private fun defaultPosFinder(
    world: ServerLevel,
    isOpenBlock: (BlockPos) -> Boolean
): (Vec3) -> BlockPos? = {
    val above = BlockPos.containing(it.add(VecUtils.yAxis.scale(14.0)))
    val groundPos = world.findGroundBelow(above)
    val up = groundPos.above()
    if (up.y + 28 >= above.y && isOpenBlock(up)) up else null
}

private fun isOpenBlock(world: ServerLevel): (BlockPos) -> Boolean = {
    world.getBlockState(it).canBeReplaced(
        DirectionalPlaceContext(
            world,
            it,
            Direction.DOWN,
            ItemStack.EMPTY,
            Direction.UP
        )
    )
}