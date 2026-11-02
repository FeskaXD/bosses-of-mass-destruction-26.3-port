package net.barribob.boss.projectile

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.obsidilith.RiftBurst
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.ModUtils.findGroundBelow
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.ModUtils.shieldPiercing
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.context.DirectionalPlaceContext
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import com.geckolib.animatable.GeoEntity
import com.geckolib.animatable.instance.AnimatableInstanceCache
import com.geckolib.animatable.manager.AnimatableManager
import com.geckolib.util.GeckoLibUtil
import java.util.function.Predicate

class SporeBallProjectile : BaseThrownItemEntity, GeoEntity {
    private val circlePoints = MathUtils.buildBlockCircle(7.0)
    private val projectileParticles = ClientParticleBuilder(Particles.DISAPPEARING_SWIRL)
        .color(ModColors.GREEN)
        .colorVariation(0.4)
        .scale(0.2f)
        .brightness(Particles.FULL_BRIGHT)
    private val particle: Byte = 5
    var impacted = false
        private set
    override val collisionPredicate = Predicate<HitResult> { true }
    private var impactedPitch = 0f
    var impactedTicks = 0f
        private set

    constructor(entityType: EntityType<out ThrowableItemProjectile>, world: Level) : super(entityType, world)

    constructor(
        livingEntity: LivingEntity,
        world: Level,
        entityPredicate: Predicate<EntityHitResult>
    ) : super(
        Entities.SPORE_BALL,
        livingEntity,
        world,
        entityPredicate
    )

    override fun onHitBlock(blockHitResult: BlockHitResult) {
        onImpact()
    }

    override fun clientTick() {
        super.clientTick()
        if (impacted) {
            impactedTicks++
        }
    }

    override fun getDeltaMovement(): Vec3 {
        return if (!impacted) {
            super.getDeltaMovement()
        } else {
            Vec3.ZERO
        }
    }

    private fun onImpact() {
        if (impacted) return

        impactedPitch = xRot
        impacted = true
        val owner = getOwner()
        if (!level().isClientSide) {
            if (owner is LivingEntity) {
                doExplosion(owner)
            } else {
                discard()
            }
        }
    }

    override fun getXRot() = if (impacted) impactedPitch else tickCount * 5f
    override fun getYRot() = 0f

    private fun doExplosion(owner: LivingEntity) {
        level().broadcastEntityEvent(this, particle)
        playSound(Mod.sounds.sporeBallLand, 1.0f, random.randomPitch() - 0.2f)
        val eventScheduler = ModComponents.getWorldEventScheduler(level())
        val onImpact: (LivingEntity) -> Unit = {
            val damage = owner.getAttributeValue(Attributes.ATTACK_DAMAGE).toFloat()
            getOwner()?.let { it1 ->
                it.hurt(level().damageSources().shieldPiercing(level(), it1), damage)
                it.addEffect(MobEffectInstance(MobEffects.POISON, 140), getOwner())
            }
        }

        val riftBurst = RiftBurst(
            owner,
            level() as ServerLevel,
            Particles.SPORE_INDICATOR,
            Particles.SPORE,
            explosionDelay,
            eventScheduler,
            onImpact,
            ::isOpenBlock,
            ::posFinder
        )

        eventScheduler.addEvent(TimedEvent({
            playSound(Mod.sounds.sporeImpact, 1.5f, random.randomPitch())
            discard()
        }, explosionDelay))

        val center = blockPosition().asVec3d().add(VecUtils.unit.scale(0.5))
        for (point in circlePoints) {
            riftBurst.tryPlaceRift(center.add(point))
        }
    }

    private fun isOpenBlock(up: BlockPos) = level().getBlockState(up).canBeReplaced(
        DirectionalPlaceContext(
            level(),
            up,
            Direction.DOWN,
            ItemStack.EMPTY,
            Direction.UP
        )
    ) || level().getBlockState(up).block == Blocks.MOSS_CARPET

    private fun posFinder(pos: Vec3): BlockPos? {
        val above = BlockPos.containing(pos.add(VecUtils.yAxis.scale(2.0)))
        val groundPos = level().findGroundBelow(above)
        val up = groundPos.above()
        return if (up.y + 8 >= above.y && isOpenBlock(up)) up else null
    }

    override fun handleEntityEvent(status: Byte) {
        if (status == particle) {
            for (point in MathUtils.circlePoints(0.8, 16, VecUtils.yAxis)) {
                projectileParticles.build(point.add(position()), point.scale(0.1))
            }
        }
        super.handleEntityEvent(status)
    }

    override fun entityHit(entityHitResult: EntityHitResult) {
        if (level().isClientSide) return
        val owner = getOwner()
        val entity = entityHitResult.entity

        if (owner is LivingEntity) {
            if (entity != owner) {
                val damage = owner.getAttributeValue(Attributes.ATTACK_DAMAGE).toFloat()
                entity.hurt(level().damageSources().thrown(this, owner), damage)
            }
        }
    }

    override fun registerControllers(data: AnimatableManager.ControllerRegistrar) {
    }

    private val animationFactory = GeckoLibUtil.createInstanceCache(this)
    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = animationFactory

    companion object {
        const val explosionDelay = 30
    }
}