package net.barribob.boss.mob.utils

import net.minecraft.world.phys.Vec3

interface IEntity {
    fun getVel(): Vec3
    fun getPos(): Vec3
    fun getEyePos(): Vec3
    fun getRotationVector(): Vec3
    fun getAge(): Int
    fun isAlive(): Boolean
    fun target(): IEntity?
}