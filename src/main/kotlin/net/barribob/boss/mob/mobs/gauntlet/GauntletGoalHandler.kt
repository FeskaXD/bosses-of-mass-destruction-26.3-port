package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.ai.goals.CompositeGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.boss.mob.utils.IMoveHandler
import net.barribob.boss.mob.utils.INbtHandler
import net.barribob.maelstrom.general.event.EventScheduler
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.ai.goal.GoalSelector
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

class GauntletGoalHandler(
    val entity: GauntletEntity,
    private val goalSelector: GoalSelector,
    private val targetSelector: GoalSelector,
    private val eventScheduler: EventScheduler,
    private val mobConfig: GauntletConfig
) : INbtHandler, IMoveHandler, IDamageHandler {
    private var isAggroed = false
    private val movementHelper = GauntletMovement(entity)

    private fun addGoals() {
        val serverWorld = entity.level()
        if (serverWorld is ServerLevel) {
            val attackHelper = GauntletAttacks(entity, eventScheduler, mobConfig, serverWorld)
            val attackGoal = CompositeGoal(movementHelper.buildAttackMovement(), attackHelper.buildAttackGoal())

            goalSelector.addGoal(2, CompositeGoal()) // Idle goal
            goalSelector.addGoal(3, attackGoal)

            targetSelector.addGoal(2, FindTargetGoal(entity, Player::class.java, { entity.boundingBox.inflate(it) }))
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putBoolean(::isAggroed.name, isAggroed)
        return tag
    }

    override fun fromTag(tag: CompoundTag) {
        if (tag.contains(::isAggroed.name)) {
            isAggroed = tag.getBooleanOr(::isAggroed.name, false)
            if (isAggroed) addGoals()
        }
    }

    override fun canMove(type: MoverType, movement: Vec3): Boolean = isAggroed

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        if (result && !isAggroed) {
            isAggroed = true
            addGoals()
        }
    }
}