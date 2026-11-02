package net.barribob.boss.projectile

import net.barribob.boss.mob.Entities
import net.barribob.boss.projectile.util.ExemptEntities
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.level.Level

class PetalBladeProjectile : BaseThrownItemEntity {
    private var entityHit: ((LivingEntity) -> Unit)? = null

    constructor(entityType: EntityType<out ThrowableItemProjectile>, world: Level) : super(entityType, world) {
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(renderRotation, 0f)
    }

    constructor(livingEntity: LivingEntity, world: Level, entityHit: (LivingEntity) -> Unit, exemptEntities: List<EntityType<*>>, rotation: Float) : super(
        Entities.PETAL_BLADE,
        livingEntity,
        world,
        ExemptEntities(exemptEntities)
    ) {
        this.entityHit = entityHit
    }

    override fun entityHit(entityHitResult: EntityHitResult) {
        val entity = entityHitResult.entity
        val owner = getOwner()
        if (owner != null && owner is LivingEntity) {
            entity.hurt(
                entity.level().damageSources().thrown(this, owner),
                owner.getAttributeValue(Attributes.ATTACK_DAMAGE).toFloat()
            )
            if (entity is LivingEntity) {
                entityHit?.let { it(entity ) }
            }
        }
        discard()
    }

    override fun onHitBlock(blockHitResult: BlockHitResult) {
        super.onHitBlock(blockHitResult)
        discard()
    }

    companion object {
        val renderRotation: EntityDataAccessor<Float> = SynchedEntityData.defineId(PetalBladeProjectile::class.java, EntityDataSerializers.FLOAT)
    }
}