package net.barribob.boss.mob.mobs.obsidilith

import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.level.storage.ValueInput
import net.barribob.boss.Mod
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.config.ObsidilithConfig
import net.barribob.boss.mob.ai.action.CooldownAction
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.damage.CompositeDamageHandler
import net.barribob.boss.mob.damage.DamageMemory
import net.barribob.boss.mob.mobs.void_blossom.CappedHeal
import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.mob.utils.StatusImmunity
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MoverType
import net.minecraft.world.BossEvent
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvent
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import com.geckolib.animatable.manager.AnimatableManager
import com.geckolib.animation.AnimationController
import com.geckolib.animation.`object`.PlayState
import com.geckolib.animation.RawAnimation

class ObsidilithEntity(
    entityType: EntityType<out ObsidilithEntity>,
    world: Level,
    private val mobConfig: ObsidilithConfig
) : BaseEntity(entityType, world) {
    override val bossBar = ServerBossEvent(java.util.UUID.randomUUID(), this.displayName, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_12)
    var currentAttack: Byte = 0
    private val statusRegistry = mapOf(
        Pair(ObsidilithUtils.burstAttackStatus, BurstAction(this)),
        Pair(ObsidilithUtils.waveAttackStatus, WaveAction(this)),
        Pair(ObsidilithUtils.spikeAttackStatus, SpikeAction(this)),
        Pair(ObsidilithUtils.anvilAttackStatus, AnvilAction(this, mobConfig.anvilAttackExplosionStrength)),
        Pair(ObsidilithUtils.pillarDefenseStatus, PillarAction(this))
    )
    private val damageMemory = DamageMemory(10, this)
    private val moveLogic = ObsidilithMoveLogic(statusRegistry, this, damageMemory)
    private val effectHandler = ObsidilithEffectHandler(this, ModComponents.getWorldEventScheduler(world))
    override val damageHandler = CompositeDamageHandler(moveLogic, ShieldDamageHandler(::isShielded), damageMemory)
    private val activePillars = mutableSetOf<BlockPos>()
    override val statusEffectHandler = StatusImmunity(MobEffects.WITHER.value(), MobEffects.POISON.value())
    override val serverTick = CappedHeal(this, ObsidilithUtils.hpPillarShieldMilestones, mobConfig.idleHealingPerTick)

    init {

        if (!world.isClientSide && world is ServerLevel) {
            goalSelector.addGoal(1, buildAttackGoal())

            targetSelector.addGoal(2, FindTargetGoal(this, Player::class.java, { boundingBox.inflate(it) }))

            preTickEvents.addEvent(TimedEvent({
                world.playSound(position(), Mod.sounds.waveIndicator, SoundSource.HOSTILE, 1.5f, 0.7f)
            }, 1))
        }
    }
    
    companion object {
        val isShielded: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(ObsidilithEntity::class.java, EntityDataSerializers.BOOLEAN)
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(isShielded, false)
    }

    private fun buildAttackGoal(): ActionGoal {
        val attackAction = CooldownAction(moveLogic, 80)
        return ActionGoal(
            ::canContinueAttack,
            tickAction = attackAction,
            endAction = attackAction
        )
    }

    override fun serverTick(serverWorld: ServerLevel) {
        super.serverTick(serverWorld)

        activePillars.removeIf {
            level().getBlockState(it).block != ModBlocks.obsidilithRune || !it.closerThan(
                blockPosition(),
                64.0
            )
        }
        getEntityData().set(isShielded, activePillars.any())

        if (this.tickCount % 40 == 0) {
            activePillars.randomOrNull()?.let {
                MathUtils.lineCallback(it.asVec3d().add(0.5, 0.5, 0.5), eyePos(), 15) { vec3d, i ->
                    preTickEvents.addEvent(TimedEvent({
                        serverWorld.spawnParticle(Particles.PILLAR_RUNE, vec3d, Vec3.ZERO)
                    }, i))
                }
            }
        }
    }

    override fun canBeCollidedWith(entity: Entity?): Boolean = true

    override fun push(entity: Entity) {
    }

    override fun die(source: DamageSource) {
        if (mobConfig.spawnPillarOnDeath) {
            ObsidilithUtils.onDeath(this, mobConfig.experienceDrop)
            if (level().isClientSide) effectHandler.handleStatus(ObsidilithUtils.deathStatus)
        }

        super.die(source)
    }

    private fun canContinueAttack() = isAlive && target != null

    override fun handleEntityEvent(status: Byte) {
        val attackStatus = statusRegistry[status]
        if (attackStatus != null) {
            effectHandler.handleStatus(status)
            currentAttack = status
            preTickEvents.addEvent(TimedEvent({ currentAttack = 0 }, 40))
        }
        super.handleEntityEvent(status)
    }

    override fun getHurtSound(source: DamageSource) = Mod.sounds.obsidilithHurt
    override fun getDeathSound(): SoundEvent = Mod.sounds.obsidilithDeath

    override fun registerControllers(data: AnimatableManager.ControllerRegistrar) {
        data.add(AnimationController<ObsidilithEntity>("summon", 0) {
            it.controller.setAnimation(
                RawAnimation.begin().thenPlay("summon")
            )
            PlayState.CONTINUE
        })
    }

    override fun move(type: MoverType, movement: Vec3) {
        super.move(type, Vec3(0.0, movement.y, 0.0))
    }

    override fun isOnFire(): Boolean {
        return false
    }

    override fun getArmorValue(): Int = if (target != null) super.getArmorValue() else 24

    override fun checkDespawn() = ModUtils.preventDespawnExceptPeaceful(this, level())

    override fun causeFallDamage(fallDistance: Double, damageMultiplier: Float, damageSource: DamageSource) = false

    fun isShielded(): Boolean = getEntityData().get(isShielded)

    fun addActivePillar(pos: BlockPos) {
        activePillars.add(pos)
    }

    override fun addAdditionalSaveData(output: ValueOutput) {
        super.addAdditionalSaveData(output)
        output.putIntArray(::activePillars.name, activePillars.flatMap { listOf(it.x, it.y, it.z) }.toIntArray())
    }

    override fun readExtraData(input: ValueInput) {
        input.getIntArray(::activePillars.name).ifPresent { array ->
            activePillars.addAll(array.asIterable().chunked(3).map { BlockPos(it[0], it[1], it[2]) })
        }
    }
}