package net.barribob.boss.mob.ai

import net.minecraft.world.phys.Vec3

interface ITargetSelector {
    fun getTarget(): Vec3
}