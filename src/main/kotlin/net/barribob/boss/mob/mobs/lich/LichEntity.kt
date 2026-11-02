package net.barribob.boss.mob.mobs.lich

import net.minecraft.core.registries.Registries
import net.minecraft.world.clock.WorldClocks
import net.minecraft.world.entity.EntityTypes
import net.barribob.boss.Mod
import net.barribob.boss.config.LichConfig
import net.barribob.boss.mob.ai.goals.CompositeGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.damage.CompositeDamageHandler
import net.barribob.boss.mob.damage.DamageMemory
import net.barribob.boss.mob.damage.DamagedAttackerNotSeen
import net.barribob.boss.mob.damage.StagedDamageHandler
import net.barribob.boss.mob.mobs.gauntlet.AnimationHolder
import net.barribob.boss.mob.mobs.lich.LichActions.Companion.hpBelowThresholdStatus
import net.barribob.boss.mob.mobs.lich.LichUtils.hpPercentRageModes
import net.barribob.boss.mob.mobs.void_blossom.CappedHeal
import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.mob.utils.CompositeEntityTick
import net.barribob.boss.mob.utils.CompositeStatusHandler
import net.barribob.boss.utils.AnimationUtils
import net.barribob.boss.utils.ModUtils
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.BossEvent
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.monster.Phantom
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import com.geckolib.animatable.manager.AnimatableManager
import com.geckolib.animation.AnimationController

class LichEntity(entityType: EntityType<out LichEntity>, world: Level, private val mobConfig: LichConfig) : BaseEntity(
    entityType,
    world
) {
    private val animationHolder = AnimationHolder(
        this, mapOf(
            Pair(LichActions.endTeleport, AnimationHolder.Animation("unteleport", "idle")),
            Pair(LichActions.cometRageAttack, AnimationHolder.Animation("rage_mode", "idle")),
            Pair(LichActions.volleyRageAttack, AnimationHolder.Animation("rage_mode", "idle")),
            Pair(LichActions.cometAttack, AnimationHolder.Animation("summon_fireball", "idle")),
            Pair(LichActions.minionAttack, AnimationHolder.Animation("summon_minions", "idle")),
            Pair(LichActions.minionRageAttack, AnimationHolder.Animation("rage_mode", "idle")),
            Pair(LichActions.teleportAction, AnimationHolder.Animation("teleport", "teleporting")),
            Pair(LichActions.volleyAttack, AnimationHolder.Animation("summon_missiles", "idle")),
            Pair(3, AnimationHolder.Animation("idle", "idle"))
        ),
        LichActions.stopAttackAnimation, 0
    )
    private val minionAction = MinionAction(this, preTickEvents, ::cancelAttackAction)
    private val teleportAction = TeleportAction(this, preTickEvents, ::cancelAttackAction)
    private val statusRegistry = mapOf(
        Pair(LichActions.cometAttack, CometAction(this, preTickEvents, ::cancelAttackAction, mobConfig)),
        Pair(LichActions.volleyAttack, VolleyAction(this, mobConfig, preTickEvents, ::cancelAttackAction)),
        Pair(LichActions.minionAttack, minionAction),
        Pair(LichActions.minionRageAttack, MinionRageAction(this, preTickEvents, ::cancelAttackAction, minionAction)),
        Pair(LichActions.teleportAction, teleportAction),
        Pair(LichActions.cometRageAttack, CometRageAction(this, preTickEvents, ::cancelAttackAction, mobConfig)),
        Pair(LichActions.volleyRageAttack, VolleyRageAction(this, mobConfig, preTickEvents, ::cancelAttackAction)),
    )
    private val damageMemory = DamageMemory(5, this)
    private val moveLogic = LichMoveLogic(statusRegistry, this, damageMemory)
    private val lichParticles = LichParticleHandler(this, preTickEvents)

    val shouldSetToNighttime = mobConfig.eternalNighttime
    val velocityHistory = HistoricalData(Vec3.ZERO)
    var collides = true

    private val cappedHeal = CappedHeal(this, hpPercentRageModes, mobConfig.idleHealingPerTick)

    override val statusHandler = CompositeStatusHandler(animationHolder, lichParticles)
    override val damageHandler = CompositeDamageHandler(
        StagedDamageHandler(hpPercentRageModes) { world.broadcastEntityEvent(this, hpBelowThresholdStatus) },
        DamagedAttackerNotSeen(this) { if(it is ServerPlayer) teleportAction.performTeleport(it) },
        moveLogic, damageMemory )
    override val bossBar = ServerBossEvent(java.util.UUID.randomUUID(), this.displayName, BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS)
    override val serverTick = CompositeEntityTick(cappedHeal, moveLogic)
    override val clientTick = lichParticles

    init {

        if (!world.isClientSide) {
            val attackHelper = LichActions(this, moveLogic)
            val moveHelper = LichMovement(this)

            goalSelector.addGoal(1, FloatGoal(this))
            goalSelector.addGoal(3, CompositeGoal(moveHelper.buildAttackMovement(), attackHelper.buildAttackGoal()))
            goalSelector.addGoal(4, moveHelper.buildWanderGoal())

            targetSelector.addGoal(2, FindTargetGoal(this, Player::class.java, { this.boundingBox.inflate(it) }))
        }
    }

    override fun registerControllers(data: AnimatableManager.ControllerRegistrar) {
        animationHolder.registerControllers(data)
        data.add(AnimationController("skull_float", 0, AnimationUtils.createIdlePredicate("skull_float")))
        data.add(AnimationController("float", 0, AnimationUtils.createIdlePredicate("float")))
        data.add(AnimationController("book_idle", 0, AnimationUtils.createIdlePredicate("book_idle")))
    }

    fun inLineOfSight(target: Entity) : Boolean {
        val hasDirectLineOfSight = VanillaCopies.hasDirectLineOfSight(eyePos(), target.eyePos(), level(), this)
        val directionToLich = MathUtils.unNormedDirection(target.eyePos(), eyePos())
        val facingSameDirection = MathUtils.facingSameDirection(target.lookAngle, directionToLich)
        return hasDirectLineOfSight && facingSameDirection
    }

    override fun clientTick() {
        velocityHistory.set(deltaMovement)
    }

    override fun serverTick(serverWorld: ServerLevel) {
        if (shouldSetToNighttime) {
            serverWorld.server.clockManager().setTotalTicks(serverWorld.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD), LichUtils.timeToNighttime(serverWorld.overworldClockTime))
        }
    }

    override fun canCollideWith(other: Entity): Boolean = collides
    override fun causeFallDamage(fallDistance: Double, damageMultiplier: Float, damageSource: DamageSource) = false
    override fun checkDespawn() = ModUtils.preventDespawnExceptPeaceful(this, level())
    override fun getHurtSound(source: DamageSource) = Mod.sounds.lichHurt
    override fun getDeathSound(): SoundEvent = Mod.sounds.lichDeath
    override fun getSoundVolume(): Float = 5.0f
    override fun onClimbable(): Boolean = false
    private fun cancelAttackAction() = isDeadOrDying || target == null

    override fun die(source: DamageSource) {
        val expTicks = 18
        val expPerTick = (mobConfig.experienceDrop / expTicks.toFloat()).toInt()
        preTickEvents.addEvent(TimedEvent({
            if (!level().isClientSide) VanillaCopies.awardExperience(expPerTick, eyePos(), level())
        }, 0, expTicks))

        val serverLevel = level() as? ServerLevel
        if (serverLevel != null) {
            level().getEntities(EntityTypes.PHANTOM, AABB(blockPosition()).inflate(100.0, 100.0, 100.0)) { true }.forEach { it.kill(serverLevel) }
        }

        super.die(source)
    }

    override fun checkFallDamage(
        heightDifference: Double,
        onGround: Boolean,
        landedState: BlockState,
        landedPosition: BlockPos,
    ) {
    }

    override fun travel(movementInput: Vec3) {
        VanillaCopies.travel(movementInput, this)
    }
}