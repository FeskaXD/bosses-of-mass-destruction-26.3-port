package net.barribob.boss.mob.utils

import net.minecraft.world.level.Level

fun interface IEntityTick<T> where T : Level {
    fun tick(world: T)
}