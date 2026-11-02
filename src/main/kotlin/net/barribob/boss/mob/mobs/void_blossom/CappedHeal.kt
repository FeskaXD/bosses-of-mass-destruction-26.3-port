package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.mob.utils.EntityAdapter
import net.barribob.boss.mob.utils.EntityStats
import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.world.entity.Mob
import net.minecraft.server.level.ServerLevel

class CappedHeal(
    private val entity: Mob,
    private val hpMilestones: List<Float>,
    private val healingPerTick: Float
) : IEntityTick<ServerLevel> {
    private val adapter = EntityAdapter(entity)
    private val stats = EntityStats(entity)

    override fun tick(world: ServerLevel) {
        if (entity.target == null) {
            LichUtils.cappedHeal(
                adapter,
                stats,
                hpMilestones,
                healingPerTick,
                entity::heal
            )
        }
    }
}