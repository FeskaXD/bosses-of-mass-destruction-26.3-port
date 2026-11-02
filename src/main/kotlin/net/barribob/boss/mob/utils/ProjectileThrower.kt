package net.barribob.boss.mob.utils

import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt

class ProjectileThrower(val projectileProvider: () -> ProjectileData) {
    fun throwProjectile(target: Vec3) {
        val projectileData = projectileProvider()
        val direction = target.subtract(projectileData.projectile.position())
        val h = sqrt(direction.x * direction.x + direction.z * direction.z) * projectileData.gravityCompensation
        projectileData.projectile.shoot(direction.x, direction.y + h, direction.z, projectileData.speed, projectileData.divergence)
        projectileData.projectile.level().addFreshEntity(projectileData.projectile)
    }
}

data class ProjectileData(val projectile: Projectile, val speed: Float, val divergence: Float, val gravityCompensation: Double = 0.2)