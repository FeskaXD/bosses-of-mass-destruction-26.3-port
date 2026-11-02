package net.barribob.boss.mob.ai.goals

import net.minecraft.world.entity.ai.goal.Goal

class CompositeGoal(vararg goals: Goal) : Goal() {
    private val goals = goals.toList()
    init {
        goals.forEach { goal: Goal -> flags.addAll(goal.flags) }
    }

    override fun canUse(): Boolean = goals.all { it.canUse() }
    override fun isInterruptable(): Boolean = goals.all { it.isInterruptable() }
    override fun requiresUpdateEveryTick() = true

    override fun tick() {
        goals.forEach { it.tick() }
    }

    override fun stop() {
        goals.forEach { it.stop() }
    }

    override fun start() {
        goals.forEach { it.start() }
    }
}