package net.barribob.boss.mob.spawn

import net.barribob.maelstrom.general.io.ILogger
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level

class CompoundTagEntityProvider(private val tag: CompoundTag, val world: Level, private val logger: ILogger): IEntityProvider {
    override fun getEntity(): Entity? {
        val entity = EntityType.loadEntityRecursive(tag, world, net.minecraft.world.entity.EntitySpawnRequest(net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED, false)) { it }

        if(entity == null) {
            logger.warn("Failed to create entity from tag: $tag")
            return null
        }

        return entity
    }

}