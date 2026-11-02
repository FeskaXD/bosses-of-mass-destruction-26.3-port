package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.config.LichConfig
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.static_utilities.VecUtils
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.stats.Stats
import net.minecraft.resources.Identifier

class LichKillCounter(private val config: LichConfig.SummonMechanic) : ServerEntityCombatEvents.AfterKilledOtherEntity {
    private val countedEntities = config.entitiesThatCountToSummonCounter?.map { BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(it)) } ?: listOf()

    override fun afterKilledOtherEntity(sWorld: ServerLevel, entity: Entity, killedEntity: LivingEntity, damageSource: net.minecraft.world.damagesource.DamageSource) {
        if (entity is ServerPlayer && killedEntity.type in countedEntities) {
            val entitiesKilled = getUndeadKilled(entity)
            if (entitiesKilled > 0 && entitiesKilled % config.numEntitiesKilledToDropSoulStar == 0) {
                sWorld.spawnParticle(Particles.SOUL_FLAME, killedEntity.position().add(VecUtils.yAxis), VecUtils.unit, 15)
                killedEntity.spawnAtLocation(sWorld, Mod.items.soulStar)
            }
        }
    }

    private fun getUndeadKilled(entity: ServerPlayer): Int =
        countedEntities.fold(0) { acc, entityType ->
            acc + entity.stats.getValue(
                Stats.ENTITY_KILLED.get(
                    entityType
                )
            )
        }
}