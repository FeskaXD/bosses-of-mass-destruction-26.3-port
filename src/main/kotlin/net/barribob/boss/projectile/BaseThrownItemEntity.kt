package net.barribob.boss.projectile

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.level.Level
import java.util.function.Predicate

abstract class BaseThrownItemEntity : ThrowableItemProjectile {
    protected val entityCollisionPredicate: Predicate<EntityHitResult>
    protected open val collisionPredicate: Predicate<HitResult> = Predicate<HitResult> { !level().isClientSide }

    constructor(
        entityType: EntityType<out ThrowableItemProjectile>, world: Level
    ) : super(entityType, world) {
        entityCollisionPredicate = Predicate { true }
    }

    constructor(
        entityType: EntityType<out ThrowableItemProjectile>,
        livingEntity: LivingEntity,
        world: Level,
        collisionPredicate: Predicate<EntityHitResult> = Predicate { true }
    ) : super(
        entityType,
        livingEntity,
        world,
        net.minecraft.world.item.ItemStack.EMPTY,
    ) {
        this.entityCollisionPredicate = collisionPredicate
        setItem(net.minecraft.world.item.ItemStack(defaultItem))
    }

    final override fun tick() {
        super.tick()
        if (level().isClientSide) {
            clientTick()
        }
    }


    override fun onHit(hitResult: HitResult) {
        if(collisionPredicate.test(hitResult)) {
            super.onHit(hitResult)
        }
    }

    final override fun onHitEntity(entityHitResult: EntityHitResult) {
        if(entityCollisionPredicate.test(entityHitResult)) {
            entityHit(entityHitResult)
        }
    }

    abstract fun entityHit(entityHitResult: EntityHitResult)

    open fun clientTick() {
    }

    override fun getDefaultItem(): Item = Items.SNOWBALL
}