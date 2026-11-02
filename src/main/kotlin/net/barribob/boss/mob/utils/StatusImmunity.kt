package net.barribob.boss.mob.utils

import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance

class StatusImmunity(vararg statusEffects: MobEffect): IStatusEffectFilter {
    private val statusEffectList = statusEffects.toList()

    override fun canHaveStatusEffect(effect: MobEffectInstance): Boolean {
        return !statusEffectList.contains(effect.effect.value())
    }
}