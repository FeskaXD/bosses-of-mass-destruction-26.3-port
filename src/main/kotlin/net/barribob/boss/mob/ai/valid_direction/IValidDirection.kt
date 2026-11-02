package net.barribob.boss.mob.ai.valid_direction

import net.minecraft.world.phys.Vec3

fun interface IValidDirection {
    fun isValidDirection(normedDirection: Vec3): Boolean
}