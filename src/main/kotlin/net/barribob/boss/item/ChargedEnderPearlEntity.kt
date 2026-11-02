package net.barribob.boss.item

import net.barribob.boss.Mod
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.NetworkUtils.Companion.sendImpactPacket
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.portal.TeleportTransition
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import kotlin.random.Random

/**
 * Sourced from [ThrownEnderpearl]
 */
class ChargedEnderPearlEntity : ThrowableItemProjectile {
    constructor(entityType: EntityType<out ChargedEnderPearlEntity>, world: Level) : super(entityType, world)
    constructor(world: Level, owner: LivingEntity) : super(Entities.CHARGED_ENDER_PEARL, owner, world, ItemStack(Mod.items.chargedEnderPearl))

    override fun getDefaultItem(): Item = Mod.items.chargedEnderPearl

    override fun onHitEntity(entityHitResult: EntityHitResult) {
        super.onHitEntity(entityHitResult)
        entityHitResult.entity.hurt(entityHitResult.entity.level().damageSources().thrown(this, getOwner()), 0.0f)
    }

    override fun onHit(hitResult: HitResult) {
        super.onHit(hitResult)
        if (!level().isClientSide) {
            if(!this.isRemoved) serverCollision()
        }
    }

    private fun serverCollision() {
        teleportEntity(getOwner())
        applyStatusEffects(getOwner())
        sendImpactPacket(level() as ServerLevel, position())
        playSound(Mod.sounds.chargedEnderPearl, 1.0f, random.randomPitch() * 0.8f)
        discard()
    }

    private fun teleportEntity(entity: Entity?) {
        if (entity is ServerPlayer) {
            if (entity.connection.isAcceptingMessages && entity.level() === level() && !entity.isSleeping) {
                if (entity.isPassenger()) {
                    entity.dismountTo(this.x, this.y, this.z)
                } else {
                    entity.teleportTo(this.x, this.y, this.z)
                }
                entity.resetFallDistance()
            }
        } else if (entity != null) {
            entity.teleportTo(this.x, this.y, this.z)
            entity.resetFallDistance()
        }
    }

    private fun applyStatusEffects(entity: Entity?) {
        if(entity is LivingEntity) {
            entity.addEffect(MobEffectInstance(MobEffects.RESISTANCE, 120, 1))
            entity.addEffect(MobEffectInstance(MobEffects.SLOW_FALLING, 20, 0))
        }
        val entities = level()
            .getEntities(entity, AABB(x, y, z, x, y, z).inflate(radius * 2, impactHeight * 2, radius * 2))
            .filterIsInstance<LivingEntity>()
            .filter(::isInXzDistance)
            .filter(::isInYDistance)
        for(e in entities) {
            val direction = position().subtract(e.position())
            e.knockback(0.4, direction.x, direction.z, damageSources().generic(), 0f)
        }
    }

    private fun isInXzDistance(entity: LivingEntity) : Boolean {
        val xzLineToEntity = entity.position().planeProject(VecUtils.yAxis).subtract(position().planeProject(VecUtils.yAxis))
        val sqXzDistanceTowardEntity = xzLineToEntity.lengthSqr()
        val entityCenterInRadius = sqXzDistanceTowardEntity < radius * radius
        if(entityCenterInRadius) return true

        val xzDirectionTowardEntity = xzLineToEntity.normalize()
        val xzPointTowardEntity = xzDirectionTowardEntity.scale(radius).add(position())
        return entity.boundingBox.inflate(0.0, 10.0, 0.0).contains(xzPointTowardEntity)
    }

    private fun isInYDistance(entity: LivingEntity) : Boolean {
        val yBottom = position().y - 1
        val yTop = position().y + impactHeight
        return entity.boundingBox.maxY > yBottom || entity.boundingBox.minY < yTop
    }

    override fun tick() {
        val entity = getOwner()
        if (entity is Player && !entity.isAlive()) {
            discard()
        } else {
            super.tick()
        }
    }

    override fun teleport(teleportTarget: TeleportTransition): Entity? {
        val entity = getOwner()
        if (entity != null && entity.level().dimension() !== teleportTarget.newLevel().dimension()) {
            setOwner(null as Entity?)
        }
        return super.teleport(teleportTarget)
    }

    companion object {
        private const val radius = 3.0
        private const val impactHeight = 3.0
        private val verticalRodParticle = ClientParticleBuilder(Particles.ROD)
            .brightness(Particles.FULL_BRIGHT)
            .color { age -> MathUtils.lerpVec(age, ModColors.LIGHT_ENDER_PEARL, ModColors.DARK_ENDER_PEARL) }
            .colorVariation(0.25)
        private val enderParticle = ClientParticleBuilder(Particles.FLUFF)
            .brightness(Particles.FULL_BRIGHT)
            .color (ModColors.ENDER_PURPLE)
            .colorVariation(0.25)

        fun handlePearlImpact(pos: Vec3) {
            spawnTeleportParticles(pos)
            spawnVerticalRods(pos)
        }

        private fun spawnTeleportParticles(pos: Vec3) {
            for(i in 0..10) {
                val startingRotation = Random.nextInt(360)
                val randomRadius = RandomUtils.range(2.0, 3.0)
                val rotationSpeed = RandomUtils.range(3.0, 4.0)
                enderParticle
                    .continuousPosition { rotateAroundPos(pos, i, it.getAge(), startingRotation, randomRadius, rotationSpeed) }
                    .build(rotateAroundPos(pos, i, 0, startingRotation, randomRadius, rotationSpeed), Vec3.ZERO)
            }
        }

        private fun rotateAroundPos(
            pos: Vec3,
            i: Int,
            age: Int,
            startingRotation: Int,
            radius: Double,
            rotationSpeed: Double
        ): Vec3 {
            val yOffset = VecUtils.yAxis.scale(i / 15.0)
            val xzOffset = VecUtils.xAxis.yRot(Math.toRadians(age * rotationSpeed + startingRotation).toFloat())
            return pos.add(yOffset).add(xzOffset.scale(radius))
        }

        private fun spawnVerticalRods(pos: Vec3) {
            for(y in 0..impactHeight.toInt()) {
                spawnVerticalRodRing(y, pos)
            }
        }

        private fun spawnVerticalRodRing(y: Int, pos: Vec3) {
            MathUtils.circleCallback(radius, 30 - (y * 3), VecUtils.yAxis) {
                verticalRodParticle
                    .build(
                        pos.add(it).add(VecUtils.yAxis.scale(y + RandomUtils.range(0.0, 1.0))),
                        VecUtils.yAxis.scale(0.1))
            }
        }
    }
}
