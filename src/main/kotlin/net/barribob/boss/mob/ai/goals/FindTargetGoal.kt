package net.barribob.boss.mob.ai.goals

import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.Mob
import net.minecraft.world.phys.AABB
import java.util.function.Predicate

class FindTargetGoal<T : LivingEntity>(
    mob: Mob,
    targetClass: Class<T>,
    private val searchBoxProvider: (Double) -> AABB,
    reciprocalChance: Int = 10,
    checkVisibility: Boolean = true,
    checkCanNavigate: Boolean = false,
    targetPredicate: Predicate<LivingEntity>? = null
) : NearestAttackableTargetGoal<T>(
    mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate,
    targetPredicate?.let { p -> TargetingConditions.Selector { e, _ -> p.test(e) } }
) {
    override fun getTargetSearchArea(distance: Double): AABB = searchBoxProvider(distance)

    init {
        unseenMemoryTicks = 200
    }
}