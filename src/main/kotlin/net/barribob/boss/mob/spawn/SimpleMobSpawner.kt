package net.barribob.boss.mob.spawn

import net.barribob.maelstrom.static_utilities.setPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.Mob
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

/**
 * Logic sourced from [MobSpawnerLogic.sculkSpreadDelay]
 */
class SimpleMobSpawner(private val serverWorld: ServerLevel) : IMobSpawner {
    override fun spawn(pos: Vec3, entity: Entity) {
        entity.setPos(pos)
        if (entity is Mob) {
            entity.finalizeSpawn(serverWorld,
                serverWorld.getCurrentDifficultyAt(entity.blockPosition()),
                EntitySpawnReason.MOB_SUMMONED,
                null)
        }

        serverWorld.addFreshEntityWithPassengers(entity)

        if (entity is Mob) {
            entity.spawnAnim()
        }
    }
}