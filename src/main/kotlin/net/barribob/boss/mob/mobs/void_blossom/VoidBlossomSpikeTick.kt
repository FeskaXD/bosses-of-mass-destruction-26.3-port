package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.AABB
import kotlin.math.pow

class VoidBlossomSpikeTick(private val entity: VoidBlossomEntity) : IEntityTick<ServerLevel>{
    override fun tick(world: ServerLevel) {
        val spikeHitbox = AABB(entity.position(), entity.position()).inflate(3.0, 3.0, 3.0).move(0.0, 1.5, 0.0)
        val targets = world.getEntitiesOfClass(LivingEntity::class.java, spikeHitbox) { it != entity }
        for(target in targets) {
            val damage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE).toFloat()
            if(target.position().distanceToSqr(entity.position()) < 3.0.pow(2)) {
                target.hurt(entity.level().damageSources().thorns(entity), damage)
            }
        }
    }
}