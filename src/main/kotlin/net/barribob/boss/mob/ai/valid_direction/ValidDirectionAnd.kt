package net.barribob.boss.mob.ai.valid_direction

import net.minecraft.world.phys.Vec3

class ValidDirectionAnd(private val validators: List<IValidDirection>) : IValidDirection{
    override fun isValidDirection(normedDirection: Vec3): Boolean = validators.all { it.isValidDirection(normedDirection) }
}