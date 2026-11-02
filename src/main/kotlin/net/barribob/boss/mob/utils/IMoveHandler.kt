package net.barribob.boss.mob.utils

import net.minecraft.world.entity.MoverType
import net.minecraft.world.phys.Vec3

interface IMoveHandler {
    fun canMove(type: MoverType, movement: Vec3): Boolean
}