package net.barribob.boss.mob.spawn

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

fun interface IMobSpawner {
    fun spawn(pos: Vec3, entity: Entity)
}