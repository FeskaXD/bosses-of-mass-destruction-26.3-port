package net.barribob.boss.mob.spawn

import net.barribob.maelstrom.general.random.IRandom
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.world.phys.Vec3

class HorizontalRangedSpawnPosition(
    private val position: Vec3,
    private val minDistance: Double,
    private val maxDistance: Double,
    private val random: IRandom,
) : ISpawnPosition {
    override fun getPos(): Vec3 {
        val randomOffset = random.getVector().normalize()
        val horizontalAddition = randomOffset.planeProject(VecUtils.yAxis).normalize()
        val coercedRandomOffset = randomOffset.add(horizontalAddition).normalize()
            .scale(randomOffset.length()
                .coerceAtLeast(minDistance)
                .coerceAtMost(maxDistance))
        return position.add(coercedRandomOffset)
    }
}