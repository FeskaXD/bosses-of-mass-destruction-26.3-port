package net.barribob.boss.mob.ai

import net.minecraft.world.phys.Vec3

interface ISteering {
    fun accelerateTo(target: Vec3): Vec3
}