package net.barribob.boss.mob.spawn

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.NaturalSpawner
import net.minecraft.world.level.LevelReader

/**
 * Logic sourced from [MobSpawnerLogic.sculkSpreadDelay] and [Mob.canSpawn] and [LivingEntity.teleport]
 */
class MobEntitySpawnPredicate(private val worldView: LevelReader) : ISpawnPredicate {
    override fun canSpawn(pos: Vec3, entity: Entity): Boolean {
        val blockPos = BlockPos.containing(pos)
        if(!worldView.hasChunkAt(blockPos)) return false

        val blockState = worldView.getBlockState(blockPos)
        val fluidState = worldView.getFluidState(blockPos)
        val prospectiveBoundingBox = entity.type.getSpawnAABB(pos.x, pos.y, pos.z)

        return (!worldView.containsAnyLiquid(prospectiveBoundingBox)
                && worldView.noCollision(prospectiveBoundingBox)
                && NaturalSpawner.isValidEmptySpawnBlock(worldView, blockPos, blockState, fluidState, entity.type))
    }
}