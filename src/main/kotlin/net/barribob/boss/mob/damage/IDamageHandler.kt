package net.barribob.boss.mob.damage

import net.barribob.boss.mob.utils.IEntityStats
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.damagesource.DamageSource

interface IDamageHandler {
    fun beforeDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {}
    fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {}
    fun shouldDamage(actor: LivingEntity, damageSource: DamageSource, amount: Float): Boolean = true
}