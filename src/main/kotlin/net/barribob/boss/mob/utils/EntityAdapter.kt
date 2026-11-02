package net.barribob.boss.mob.utils

import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.phys.Vec3

class EntityAdapter(val entity: LivingEntity): IEntity {
    override fun getVel(): Vec3 = entity.deltaMovement
    override fun getPos(): Vec3 = entity.position()
    override fun getEyePos() = entity.eyePos()
    override fun getRotationVector(): Vec3 = entity.lookAngle
    override fun getAge(): Int = entity.tickCount
    override fun isAlive(): Boolean = entity.isAlive
    override fun target(): IEntity? {
        if(entity is Mob) {
            val target = entity.target
            if(target != null) {
                return EntityAdapter(target)
            }
        }

        return null
    }
}