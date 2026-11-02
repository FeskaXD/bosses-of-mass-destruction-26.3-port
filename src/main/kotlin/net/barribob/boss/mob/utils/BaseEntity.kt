package net.barribob.boss.mob.utils

import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.maelstrom.general.event.EventScheduler
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MoverType
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import org.jetbrains.annotations.Nullable
import com.geckolib.animatable.GeoEntity
import com.geckolib.animatable.instance.AnimatableInstanceCache
import com.geckolib.util.GeckoLibUtil

abstract class BaseEntity(entityType: EntityType<out PathfinderMob>, world: Level) :
    PathfinderMob(entityType, world), GeoEntity {
    private val animationFactory: AnimatableInstanceCache by lazy { GeckoLibUtil.createInstanceCache(this) }
    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = animationFactory
    var idlePosition: Vec3 = Vec3.ZERO
    protected open val bossBar: ServerBossEvent? = null
    protected open val damageHandler: IDamageHandler? = null
    protected open val statusHandler: IStatusHandler? = null
    protected open val clientTick: IEntityTick<ClientLevel>? = null
    protected open val serverTick: IEntityTick<ServerLevel>? = null
    protected open val trackedDataHandler: ITrackedDataHandler? = null
    protected open val statusEffectHandler: IStatusEffectFilter? = null
    protected open val moveHandler: IMoveHandler? = null
    protected open val nbtHandler: INbtHandler? = null
    protected open val deathClientTick: IEntityTick<ClientLevel>? = null
    protected open val deathServerTick: IEntityTick<ServerLevel>? = null
    protected val preTickEvents = EventScheduler()
    protected val postTickEvents = EventScheduler()

    final override fun tick() {
        preTickEvents.updateEvents()
        if (idlePosition == Vec3.ZERO) idlePosition = position()
        val sidedWorld = level()
        if (sidedWorld.isClientSide && sidedWorld is ClientLevel) {
            clientTick()
            clientTick?.tick(sidedWorld)
        } else if (sidedWorld is ServerLevel) {
            serverTick(sidedWorld)
            serverTick?.tick(sidedWorld)
        }
        super.tick()
        postTickEvents.updateEvents()
    }

    override fun tickDeath() {
        val sidedWorld = level()
        if (sidedWorld.isClientSide && sidedWorld is ClientLevel && deathClientTick != null) {
            deathClientTick?.tick(sidedWorld)
        } else if (sidedWorld is ServerLevel && deathServerTick != null) {
            deathServerTick?.tick(sidedWorld)
        } else {
            super.tickDeath()
        }
    }

    open fun clientTick() {} // Todo: this may not be the best pattern to use
    open fun serverTick(serverWorld: ServerLevel) {}

    override fun aiStep() {
        super.aiStep()
        sensing.tick()
    }

    override fun customServerAiStep(level: ServerLevel) {
        super.customServerAiStep(level)
        bossBar?.progress = this.health / this.maxHealth
    }

    final override fun setCustomName(@Nullable name: Component?) {
        super.setCustomName(name)
        bossBar?.name = this.displayName
    }

    override fun startSeenByPlayer(player: ServerPlayer) {
        super.startSeenByPlayer(player)
        bossBar?.addPlayer(player)
    }

    override fun stopSeenByPlayer(player: ServerPlayer) {
        super.stopSeenByPlayer(player)
        bossBar?.removePlayer(player)
    }

    // 26.3: entity saves go through ValueInput/ValueOutput; BOMD's handlers still work on a CompoundTag stored under one key
    final override fun readAdditionalSaveData(input: ValueInput) {
        super.readAdditionalSaveData(input)
        if (hasCustomName()) {
            bossBar?.name = this.displayName
        }
        input.read(BOMD_DATA, CompoundTag.CODEC).ifPresent { nbtHandler?.fromTag(it) }
        readExtraData(input)
    }

    protected open fun readExtraData(input: ValueInput) {}

    override fun addAdditionalSaveData(output: ValueOutput) {
        super.addAdditionalSaveData(output)
        val handler = nbtHandler ?: return
        output.store(BOMD_DATA, CompoundTag.CODEC, handler.toTag(CompoundTag()))
    }

    // Todo: Make handler hooks final [handleStatus, onTrackedDataSet, move, fromTag, toTag]
    override fun handleEntityEvent(status: Byte) {
        statusHandler?.handleClientStatus(status)
        super.handleEntityEvent(status)
    }

    override fun onSyncedDataUpdated(data: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(data)
        trackedDataHandler?.onTrackedDataSet(data)
    }

    final override fun canBeAffected(effect: MobEffectInstance): Boolean {
        val statusEffectHandler = statusEffectHandler
        if (statusEffectHandler != null) return statusEffectHandler.canHaveStatusEffect(effect)
        return super.canBeAffected(effect)
    }

    final override fun hurtServer(level: ServerLevel, source: DamageSource, amount: Float): Boolean {
        val stats = EntityStats(this)
        val handler = damageHandler
        handler?.beforeDamage(stats, source, amount)
        val result = if (handler != null) {
            handler.shouldDamage(this, source, amount) && super.hurtServer(level, source, amount)
        } else {
            super.hurtServer(level, source, amount)
        }
        handler?.afterDamage(stats, source, amount, result)
        return result
    }

    final override fun setTarget(target: LivingEntity?) {
        if (target == null) idlePosition = position()
        super.setTarget(target)
    }

    override fun move(type: MoverType, movement: Vec3) {
        val shouldDoDefault = moveHandler?.canMove(type, movement) == true
        if (moveHandler == null || shouldDoDefault) super.move(type, movement)
    }


    fun safeGetTargetPos(): Vec3 {
        val target = target
        return if (target == null) Vec3.ZERO else target.position()
    }
}
private const val BOMD_DATA = "bosses_of_mass_destruction"
