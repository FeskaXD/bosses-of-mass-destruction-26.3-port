package net.barribob.boss.mob.ai.valid_direction

import net.minecraft.world.phys.Vec3

class ValidDirectionOr(private val validators: List<IValidDirection>) : IValidDirection{
    override fun isValidDirection(normedDirection: Vec3): Boolean = validators.any { it.isValidDirection(normedDirection) }
}