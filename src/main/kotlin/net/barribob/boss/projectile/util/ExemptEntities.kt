package net.barribob.boss.projectile.util

import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.EntityHitResult
import java.util.function.Predicate

class ExemptEntities(private val exemptEntities: List<EntityType<*>>): Predicate<EntityHitResult> {
    override fun test(t: EntityHitResult): Boolean =
        !exemptEntities.contains(t.entity.type)
}