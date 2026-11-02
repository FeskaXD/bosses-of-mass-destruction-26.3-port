package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.utils.ModUtils.randomPitch
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.tags.DamageTypeTags

class ShieldDamageHandler(val isShielded: () -> Boolean) : IDamageHandler {
    /**
     * Shield mechanic of [LivingEntity.baseDamage]
     */
    override fun shouldDamage(actor: LivingEntity, damageSource: DamageSource, amount: Float): Boolean {
        if(isShielded() && !damageSource.`is`(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (!damageSource.`is`(DamageTypeTags.IS_PROJECTILE)) {
                val entity: Entity? = damageSource.directEntity
                if (entity is LivingEntity) {
                    entity.knockback(0.5, actor.x - entity.getX(), actor.z - entity.getZ(), entity.damageSources().generic(), 0f)
                }
            }
            actor.playSound(Mod.sounds.energyShield, 1.0f, actor.random.randomPitch())
            return false
        }
        return true
    }
}