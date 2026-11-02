package net.barribob.boss.mob.mobs.gauntlet

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartAwareEntity
import io.github.stuff_stuffs.multipart_entities.common.util.CompoundOrientedBox
import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.damage.CompositeDamageHandler
import net.barribob.boss.mob.damage.DamageMemory
import net.barribob.boss.mob.utils.*
import net.barribob.boss.utils.ModUtils
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.BossEvent
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import com.geckolib.animatable.manager.AnimatableManager

class GauntletEntity(entityType: EntityType<out PathfinderMob>, world: Level, mobConfig: GauntletConfig) :
    BaseEntity(entityType, world),
    MultipartAwareEntity {
    val hitboxHelper = GauntletHitboxes(this)
    val laserHandler = GauntletClientLaserHandler(this, postTickEvents)
    val energyShieldHandler = GauntletClientEnergyShieldHandler(this, postTickEvents)
    val clientBlindnessHandler = GauntletBlindnessIndicatorParticles(this, preTickEvents)
    val damageMemory = DamageMemory(5, this)
    private val gauntletGoalHandler = GauntletGoalHandler(this, goalSelector, targetSelector, postTickEvents, mobConfig)
    private val animationHandler = AnimationHolder(
        this, mapOf(
            Pair(GauntletAttacks.punchAttack, AnimationHolder.Animation("punch_start", "punch_loop")),
            Pair(GauntletAttacks.stopPunchAnimation, AnimationHolder.Animation("punch_stop", "idle")),
            Pair(GauntletAttacks.stopPoundAnimation, AnimationHolder.Animation("pound_stop", "idle")),
            Pair(GauntletAttacks.laserAttack, AnimationHolder.Animation("laser_eye_start", "laser_eye_loop")),
            Pair(GauntletAttacks.laserAttackStop, AnimationHolder.Animation("laser_eye_stop", "idle")),
            Pair(GauntletAttacks.swirlPunchAttack, AnimationHolder.Animation("swirl_punch", "idle")),
            Pair(GauntletAttacks.blindnessAttack, AnimationHolder.Animation("cast", "idle")),
            Pair(3, AnimationHolder.Animation("death", "idle"))
        ),
        GauntletAttacks.stopAttackAnimation,
        0
    )
    override val damageHandler = CompositeDamageHandler(hitboxHelper, gauntletGoalHandler, damageMemory)
    override val statusHandler = CompositeStatusHandler(animationHandler, laserHandler, clientBlindnessHandler)
    override val trackedDataHandler = CompositeTrackedDataHandler(laserHandler, energyShieldHandler)
    override val clientTick = laserHandler
    override val serverTick = IEntityTick<ServerLevel> { if (target == null) heal(mobConfig.idleHealingPerTick) }
    override val bossBar: ServerBossEvent = ServerBossEvent(java.util.UUID.randomUUID(), displayName, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_6)
    override val statusEffectHandler = StatusImmunity(MobEffects.WITHER.value(), MobEffects.POISON.value())
    override val moveHandler = gauntletGoalHandler
    override val nbtHandler = gauntletGoalHandler
    override val deathClientTick = ClientGauntletDeathHandler(this)
    override val deathServerTick = ServerGauntletDeathHandler(this, ModComponents.getWorldEventScheduler(world), mobConfig)

    init {
    }
    
    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(laserTarget, 0)
        builder.define(isEnergized, false)
    }

    override fun registerControllers(p0: AnimatableManager.ControllerRegistrar) {
        animationHandler.registerControllers(p0)
    }

    override fun checkFallDamage(
        heightDifference: Double,
        onGround: Boolean,
        landedState: BlockState,
        landedPosition: BlockPos,
    ) {
    }

    override fun travel(movementInput: Vec3) {
        VanillaCopies.travel(movementInput, this, 0.85f)
    }

    override fun setNextDamagedPart(part: String?) {
        hitboxHelper.setNextDamagedPart(part)
    }

    override fun onSetPos(x: Double, y: Double, z: Double) {
        if (hitboxHelper != null) hitboxHelper.updatePosition()
    }

    override fun onClimbable(): Boolean = false
    override fun causeFallDamage(fallDistance: Double, damageMultiplier: Float, damageSource: DamageSource) = false
    override fun getMaxHeadXRot(): Int = 90
    override fun getCompoundBoundingBox(box: AABB): CompoundOrientedBox = hitboxHelper.getHitbox().getBox(box)
    override fun getBounds(): EntityBounds = hitboxHelper.getHitbox()
    override fun isInWall(): Boolean = false
    override fun getArmorValue(): Int = if (target != null) super.getArmorValue() else 24
    override fun getAmbientSound() = Mod.sounds.gauntletIdle
    override fun getHurtSound(source: DamageSource) = Mod.sounds.gauntletHurt
    override fun getDeathSound() = Mod.sounds.gauntletDeath
    override fun getSoundVolume() = 2.0f
    override fun checkDespawn() = ModUtils.preventDespawnExceptPeaceful(this, level())

    companion object {
        val laserTarget: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(GauntletEntity::class.java, EntityDataSerializers.INT)
        val isEnergized: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(GauntletEntity::class.java, EntityDataSerializers.BOOLEAN)
    }
}
