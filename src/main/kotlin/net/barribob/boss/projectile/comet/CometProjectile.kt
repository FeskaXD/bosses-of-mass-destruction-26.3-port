package net.barribob.boss.projectile.comet

import net.barribob.boss.mob.Entities
import net.barribob.boss.projectile.BaseThrownItemEntity
import net.barribob.boss.projectile.util.ExemptEntities
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import com.geckolib.animatable.GeoEntity
import com.geckolib.animatable.instance.AnimatableInstanceCache
import com.geckolib.animatable.manager.AnimatableManager
import com.geckolib.util.GeckoLibUtil

class CometProjectile : BaseThrownItemEntity, GeoEntity {
    private var impacted: Boolean = false
    private var impactAction: ((Vec3) -> Unit)? = null

    constructor(entityType: EntityType<out ThrowableItemProjectile>, world: Level) : super(entityType, world)

    constructor(livingEntity: LivingEntity, world: Level, impactAction: (Vec3) -> Unit, exemptEntities: List<EntityType<*>>) : super(
        Entities.COMET,
        livingEntity,
        world,
        ExemptEntities(exemptEntities)
    ) {
        this.impactAction = impactAction
    }

    private fun onImpact() {
        if (impacted) return

        impacted = true
        val owner = getOwner()
        if (owner != null && owner is LivingEntity) {
            impactAction?.let { it(position()) }
            discard()
        }
    }

    override fun entityHit(entityHitResult: EntityHitResult) {
        onImpact()
    }

    override fun onHitBlock(blockHitResult: BlockHitResult) {
        onImpact()
        super.onHitBlock(blockHitResult)
    }

    override fun hurtServer(level: net.minecraft.server.level.ServerLevel, source: DamageSource, amount: Float): Boolean {
        onImpact()
        return super.hurtServer(level, source, amount)
    }

    override fun canCollideWith(other: Entity): Boolean {
        return true
    }

    override fun registerControllers(data: AnimatableManager.ControllerRegistrar) {
    }

    private val animationFactory = GeckoLibUtil.createInstanceCache(this)
    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = animationFactory

    override fun getXRot() = tickCount * 5f
}