package net.barribob.boss.mob.ai

import net.barribob.boss.mob.damage.DamageMemory
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.Mob

class TargetSwitcher(private val entity: Mob, private val damageMemory : DamageMemory) {
    private val ageToRemember = 20 * 30

    fun trySwitchTarget() {
        val newTarget = damageMemory.getDamageHistory()
            .filter (::filterTargetableEntities)
            .filter { h -> h.ageWhenDamaged + ageToRemember >= entity.tickCount }
            .groupBy { h -> h.source.entity }
            .maxByOrNull { kv -> kv.value.sumOf { h -> h.amount.toDouble() } }?.key

        if(newTarget != null && newTarget != entity.target && entity.random.nextInt(2) == 0) {
            entity.target = newTarget as LivingEntity
        }
    }

    private fun filterTargetableEntities(damageHistory: DamageMemory.DamageHistory): Boolean {
        val attacker = damageHistory.source.entity
        if (attacker is LivingEntity) {
            val canSee = entity.sensing.hasLineOfSight(attacker)
            val followRange = entity.getAttributeValue(Attributes.FOLLOW_RANGE)
            val inRange = entity.distanceToSqr(attacker) < followRange * followRange
            return canSee && inRange && entity.canAttack(attacker)
        }

        return false
    }
}