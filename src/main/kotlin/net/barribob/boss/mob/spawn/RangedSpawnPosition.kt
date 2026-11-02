package net.barribob.boss.mob.spawn

import net.barribob.maelstrom.general.random.IRandom
import net.minecraft.world.phys.Vec3

class RangedSpawnPosition(
    private val position: Vec3,
    private val minDistance: Double,
    private val maxDistance: Double,
    private val random: IRandom,
) : ISpawnPosition {
    override fun getPos(): Vec3 {
        val randomOffset = random.getVector()
        val coercedRandomOffset = randomOffset.normalize()
            .scale(randomOffset.length()
                .coerceAtLeast(minDistance)
                .coerceAtMost(maxDistance))
        return position.add(coercedRandomOffset)
    }
}