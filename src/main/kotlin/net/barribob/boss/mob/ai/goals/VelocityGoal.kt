package net.barribob.boss.mob.ai.goals

import net.barribob.boss.mob.ai.ISteering
import net.barribob.boss.mob.ai.ITargetSelector
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.phys.Vec3
import java.util.*

class VelocityGoal(
    private val onTargetSelected: (Vec3) -> Unit,
    private val steering: ISteering,
    private val targetSelector: ITargetSelector
) : Goal() {
    init {
        flags = EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)
    }

    override fun canUse(): Boolean = true

    override fun tick() {
        val target = targetSelector.getTarget()
        val velocity = steering.accelerateTo(target)
        onTargetSelected(velocity)

        super.tick()
    }
}