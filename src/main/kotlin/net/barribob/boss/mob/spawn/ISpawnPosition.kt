package net.barribob.boss.mob.spawn

import net.minecraft.world.phys.Vec3

fun interface ISpawnPosition {
    fun getPos(): Vec3
}