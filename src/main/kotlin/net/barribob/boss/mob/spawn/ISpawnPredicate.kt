package net.barribob.boss.mob.spawn

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

fun interface ISpawnPredicate {
    fun canSpawn(pos: Vec3, entity: Entity): Boolean
}