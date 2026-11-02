package net.barribob.boss.mob.damage

import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.maelstrom.general.data.HistoricalData
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.damagesource.DamageSource

class DamageMemory(hitsToRemember: Int, private val entity: LivingEntity) : IDamageHandler {
    private val defaultDamageHistory = DamageHistory(0f, entity.level().damageSources().fellOutOfWorld(), 0)
    private val historicalData = HistoricalData(defaultDamageHistory, hitsToRemember)

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        val minimumDamageToNotice = 4
        if(result && damageSource.entity != null && amount > minimumDamageToNotice) {
            historicalData.set(DamageHistory(amount, damageSource, entity.tickCount))
        }
    }

    fun getDamageHistory() = historicalData.getAll()

    data class DamageHistory(val amount: Float, val source: DamageSource, val ageWhenDamaged: Int)
}