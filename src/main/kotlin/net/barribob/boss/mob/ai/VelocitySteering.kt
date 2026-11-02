package net.barribob.boss.mob.ai

import net.barribob.boss.mob.utils.IEntity
import net.minecraft.world.phys.Vec3

// https://gamedevelopment.tutsplus.com/tutorials/understanding-steering-behaviors-seek--gamedev-849
class VelocitySteering(private val entity: IEntity, private val maxVelocity: Double, mass: Double) :
    ISteering {

    private val inverseMass = if (mass == 0.0) throw IllegalArgumentException("Mass cannot be zero") else 1 / mass

    override fun accelerateTo(target: Vec3): Vec3 = target
        .subtract(entity.getPos())
        .normalize()
        .scale(maxVelocity)
        .subtract(entity.getVel())
        .scale(inverseMass)
}