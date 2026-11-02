package net.barribob.boss.mob.spawn

import net.minecraft.world.entity.Entity

fun interface IEntityProvider {
    fun getEntity(): Entity?
}