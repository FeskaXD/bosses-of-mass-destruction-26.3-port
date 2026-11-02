package net.barribob.boss.mob.utils

import net.minecraft.world.effect.MobEffectInstance

interface IStatusEffectFilter {
    fun canHaveStatusEffect(effect: MobEffectInstance): Boolean
}