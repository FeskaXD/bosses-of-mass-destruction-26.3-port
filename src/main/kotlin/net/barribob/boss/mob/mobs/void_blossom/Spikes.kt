package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.utils.ModUtils.findGroundBelow
import net.barribob.boss.utils.ModUtils.shieldPiercing
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.context.DirectionalPlaceContext
import net.minecraft.world.item.ItemStack
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

class Spikes(
    val entity: LivingEntity,
    val world: ServerLevel,
    private val indicatorParticle: ParticleOptions,
    private val riftTime: Int,
    val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) {
    fun tryPlaceRift(pos: Vec3): List<BlockPos> {
        return (0..12 step 6).mapNotNull {
            val above = BlockPos.containing(pos.add(VecUtils.yAxis.scale(it.toDouble())))
            val groundPos = world.findGroundBelow(above, ::isOpenBlock)
            val up = groundPos.above()
            if (up.y + 5 >= above.y) {
                placeRift(up)
                groundPos
            } else {
                null
            }

        }
    }

    private fun placeRift(pos: BlockPos) {
        world.spawnParticle(
            indicatorParticle,
            pos.asVec3d().add(Vec3(0.5, 0.1, 0.5)),
            Vec3.ZERO
        )

        eventScheduler.addEvent(TimedEvent({
            val box = AABB(pos).inflate(.0, 4.0, .0)
            val entities = world.getEntitiesOfClass(LivingEntity::class.java, box) { it != entity }

            entities.forEach {
                if (it != entity) {
                    damageEntity(it)
                }
            }
        }, riftTime, shouldCancel = shouldCancel))
    }

    private fun damageEntity(it: LivingEntity) {
        val damage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE).toFloat()
        it.hurt(entity.level().damageSources().shieldPiercing(entity.level(), entity), damage)
    }

    private fun isOpenBlock(up: BlockPos) = world.getBlockState(up).canBeReplaced(
        DirectionalPlaceContext(
            world,
            up,
            Direction.DOWN,
            ItemStack.EMPTY,
            Direction.UP
        )
    ) || world.getBlockState(up).block == Blocks.MOSS_CARPET
}