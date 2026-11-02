package net.barribob.boss.mob.mobs.void_blossom

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartAwareEntity
import io.github.stuff_stuffs.multipart_entities.common.util.CompoundOrientedBox
import net.barribob.boss.Mod
import net.barribob.boss.config.VoidBlossomConfig
import net.barribob.boss.mob.ai.TargetSwitcher
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.ai.goals.CompositeGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.damage.CompositeDamageHandler
import net.barribob.boss.mob.damage.DamageMemory
import net.barribob.boss.mob.damage.StagedDamageHandler
import net.barribob.boss.mob.mobs.gauntlet.AnimationHolder
import net.barribob.boss.mob.mobs.void_blossom.hitbox.HitboxId
import net.barribob.boss.mob.mobs.void_blossom.hitbox.NetworkedHitboxManager
import net.barribob.boss.mob.mobs.void_blossom.hitbox.VoidBlossomHitboxes
import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.mob.utils.CompositeEntityTick
import net.barribob.boss.mob.utils.CompositeStatusHandler
import net.barribob.boss.utils.ModUtils
import net.barribob.maelstrom.general.data.BooleanFlag
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MoverType
import net.minecraft.world.BossEvent
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import com.geckolib.animatable.manager.AnimatableManager

class VoidBlossomEntity(entityType: EntityType<out PathfinderMob>, world: Level, config: VoidBlossomConfig) : BaseEntity(entityType, world),
    MultipartAwareEntity {
    private val animationHolder = AnimationHolder(
        this, mapOf(
            Pair(VoidBlossomAttacks.spikeAttack, AnimationHolder.Animation("spike", "idle")),
            Pair(VoidBlossomAttacks.spikeWaveAttack, AnimationHolder.Animation("spike_wave", "idle")),
            Pair(VoidBlossomAttacks.sporeAttack, AnimationHolder.Animation("spore", "idle")),
            Pair(VoidBlossomAttacks.bladeAttack, AnimationHolder.Animation("leaf_blade", "idle")),
            Pair(VoidBlossomAttacks.blossomAction, AnimationHolder.Animation("blossom", "idle")),
            Pair(VoidBlossomAttacks.spawnAction, AnimationHolder.Animation("spawn", "idle")),
            Pair(3, AnimationHolder.Animation("death", "idle")),
        ),
        VoidBlossomAttacks.stopAttackAnimation,
        0
    )
    private val hitboxes = VoidBlossomHitboxes(this)
    private val hitboxHelper = NetworkedHitboxManager(this, hitboxes.getMap(), hitboxTrackedData)
    override val statusHandler = CompositeStatusHandler(
        animationHolder,
        ClientSporeEffectHandler(this, preTickEvents),
        ClientDeathEffectHandler(this, preTickEvents)
    )
    private val damageMemory = DamageMemory(10, this)
    private val targetSwitcher = TargetSwitcher(this, damageMemory)
    private var shouldSpawnBlossoms = BooleanFlag()
    private val hpDetector = StagedDamageHandler(hpMilestones) { shouldSpawnBlossoms.flag() }
    override val bossBar = ServerBossEvent(java.util.UUID.randomUUID(), this.displayName, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.NOTCHED_12)
    val clientSpikeHandler = VoidBlossomClientSpikeHandler()
    override val clientTick = clientSpikeHandler
    override val serverTick = CompositeEntityTick(
        LightBlockPlacer(this),
        VoidBlossomSpikeTick(this),
        hitboxes.getTickers(),
        CappedHeal(this, hpMilestones, config.idleHealingPerTick))
    override val deathServerTick = CompositeEntityTick(
        LightBlockRemover(this),
        VoidBlossomDropExpDeathTick(this, preTickEvents, config.experienceDrop)
    )
    override val damageHandler = CompositeDamageHandler(hpDetector, hitboxes.getDamageHandlers(), damageMemory)
    init {

        if (!world.isClientSide && world is ServerLevel) {
            val attackHandler = VoidBlossomAttacks(this, preTickEvents, { shouldSpawnBlossoms.getAndReset() }, targetSwitcher)
            goalSelector.addGoal(2, CompositeGoal()) // Idle goal
            goalSelector.addGoal(1, CompositeGoal(attackHandler.buildAttackGoal(), ActionGoal(::canContinueAttack, tickAction = ::lookAtTarget)))
            targetSelector.addGoal(2, FindTargetGoal(this, Player::class.java, { boundingBox.inflate(it) }))
            preTickEvents.addEvent(TimedEvent({
                playSound(Mod.sounds.spikeWaveIndicator, 2.0f, 0.7f)
            }, 1))
        }
        else if(world.isClientSide)
        {
            animationHolder.handleClientStatus(VoidBlossomAttacks.spawnAction)
        }
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(hitboxTrackedData, HitboxId.Idle.id)
    }

    private fun lookAtTarget() {
        val target = target
        if (target != null) {
            lookControl.setLookAt(target.eyePos())
            lookAt(target, headRotSpeed.toFloat(), maxHeadXRot.toFloat())
        }
    }

    override fun registerControllers(data: AnimatableManager.ControllerRegistrar) {
        animationHolder.registerControllers(data)
    }

    override fun move(type: MoverType, movement: Vec3) {
        super.move(type, Vec3(0.0, movement.y, 0.0))
    }

    private fun canContinueAttack() = isAlive && target != null
    override fun isOnFire() = false
    override fun getCompoundBoundingBox(bounds: AABB?): CompoundOrientedBox = hitboxHelper.getBounds().getBox(bounds)
    override fun getBounds(): EntityBounds = hitboxHelper.getBounds()
    override fun isInWall(): Boolean = false
    override fun getHurtSound(source: DamageSource): SoundEvent = Mod.sounds.voidBlossomHurt
    override fun getDeathSound(): SoundEvent = Mod.sounds.voidBlossomHurt
    override fun getSoundVolume(): Float = 1.5f
    override fun checkDespawn() = ModUtils.preventDespawnExceptPeaceful(this, level())
    override fun getArmorValue(): Int = if (target != null) super.getArmorValue() else 20

    override fun onSetPos(x: Double, y: Double, z: Double) {
        if (hitboxHelper != null) hitboxHelper.updatePosition()
    }

    override fun setNextDamagedPart(part: String?) {
        hitboxHelper.setNextDamagedPart(part)
    }

    companion object {
        val hpMilestones = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
        val hitboxTrackedData: EntityDataAccessor<Byte> = SynchedEntityData.defineId(VoidBlossomEntity::class.java, EntityDataSerializers.BYTE)
    }
}