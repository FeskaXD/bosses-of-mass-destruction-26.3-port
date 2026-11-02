package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.mob.utils.IEntity
import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

object LichUtils {
    val hpPercentRageModes = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
    const val textureSize = 256
    val blueColorFade: (Float) -> Vec3 =
        { MathUtils.lerpVec(it, ModColors.COMET_BLUE, ModColors.FADED_COMET_BLUE) }

    /**
     * Info from [ServerLevel.tick]
     */
    fun timeToNighttime(currentTime: Long): Long {
        val dayLength = 24000L
        val midnight = 16000L
        return (currentTime - (currentTime % dayLength)) + midnight
    }

    fun cappedHeal(
        iEntity: IEntity,
        stats: IEntityStats,
        hpPercentRageModes: List<Float>,
        healingStrength: Float,
        heal: (Float) -> Unit,
    ) {
        if (iEntity.isAlive()) {
            val targetHealthRatio = MathUtils.roundedStep(stats.getHealth() / stats.getMaxHealth(), hpPercentRageModes)
            val healAmt = Mth.clamp(targetHealthRatio * stats.getMaxHealth() - stats.getHealth() - 1, 0f, healingStrength)

            if (healAmt > 0) {
                heal(healAmt)
            }
        }
    }
}