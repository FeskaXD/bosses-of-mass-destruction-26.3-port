package net.barribob.boss.mob.mobs.gauntlet

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import net.barribob.boss.Mod
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.NetworkUtils.Companion.changeHitbox
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.tags.DamageTypeTags
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3


class GauntletHitboxes(val entity: GauntletEntity) : IDamageHandler {
    private val collisionHitbox = AABB(Vec3.ZERO, Vec3(2.0, 4.0, 2.0))
    private val clampedCollisionHitbox = AABB(Vec3.ZERO, Vec3(2.0, 2.0, 2.0))
    private val rootBoxPitch = "rootPitch"
    private val rootBoxYaw = "rootYaw"
    private var nextDamagedPart: String? = null
    private val eyeBox = "eye"
    private val fingersBox = "fingers"
    private val thumbBox = "thumb"
    private val pinkyBox = "pinky"
    private val hitboxes: EntityBounds = EntityBounds.builder()
        .add(rootBoxYaw).setBounds(0.0, 0.0, 0.0).build()
        .add(rootBoxPitch).setBounds(2.0, 2.6, 0.6).setStructurePos(0.0, 1.3, 0.0).setPos(0.0, -0.2, 0.0).setParent(rootBoxYaw).build()
        .add(eyeBox).setBounds(1.1, 1.2, 0.2).setStructurePos(-0.025, 0.35, 0.4).setParent(rootBoxPitch).build()
        .add(fingersBox).setBounds(1.5, 2.0, 0.5).setStructurePos(0.0, 1.8, 0.5).setParent(rootBoxPitch).build()
        .add(thumbBox).setBounds(0.3, 1.6, 0.3).setStructurePos(1.0, 0.6, 0.7).setParent(rootBoxPitch).build()
        .add(pinkyBox).setBounds(0.25, 1.0, 0.25).setStructurePos(-0.9, 1.7, 0.5).setParent(rootBoxPitch).build()
        .overrideCollisionBox(collisionHitbox)
        .asProvider().create()
    private val rootFistBox = "rootFist"
    private val rootFistBoxYaw = "rootFistYaw"
    private val clampedHitboxes: EntityBounds = EntityBounds.builder()
        .add(rootFistBoxYaw).setBounds(0.0, 0.0, 0.0).build()
        .add(rootFistBox).setBounds(2.0, 1.5, 2.0).setStructurePos(0.0, 1.0, 0.0).setParent(rootFistBoxYaw).build()
        .overrideCollisionBox(clampedCollisionHitbox)
        .asProvider().create()
    private var currentHitbox = hitboxes

    fun setOpenHandHitbox() {
        if (!entity.level().isClientSide && currentHitbox != hitboxes) entity.changeHitbox(true)
        currentHitbox = hitboxes
    }

    fun setClosedFistHitbox() {
        if (!entity.level().isClientSide && currentHitbox != clampedHitboxes) entity.changeHitbox(false)
        currentHitbox = clampedHitboxes
    }

    fun getHitbox(): EntityBounds = currentHitbox

    init {
        hitboxes.root(fingersBox).setRotation(35.0, 0.0, 0.0, true)
        hitboxes.root(thumbBox).setRotation(30.0, 0.0, 0.0, true)
        hitboxes.root(pinkyBox).setRotation(35.0, 0.0, 0.0, true)
    }

    fun updatePosition() {
        val rootPitch = hitboxes.root(rootBoxPitch)
        val rootYaw = hitboxes.root(rootBoxYaw)

        rootYaw.setRotation(0.0, -entity.yRot.toDouble(), 0.0, true)
        rootPitch.setRotation(entity.xRot.toDouble(), 0.0, 0.0, true)

        rootYaw.setX(entity.x)
        rootYaw.setY(entity.y)
        rootYaw.setZ(entity.z)

        val fistYaw = clampedHitboxes.root(rootFistBoxYaw)
        val fist = clampedHitboxes.root(rootFistBox)

        fistYaw.setRotation(0.0, -entity.yRot.toDouble(), 0.0, true)
        fist.setRotation(entity.xRot.toDouble(), 0.0, 0.0, true)

        fistYaw.setX(entity.x)
        fistYaw.setY(entity.y)
        fistYaw.setZ(entity.z)

        val overrideBox = hitboxes.overrideBox
        if (overrideBox != null) overrideBox.box = collisionHitbox.move(entity.position()).move(-1.0, 0.0, -1.0)

        val overrideClampedHitbox = clampedHitboxes.overrideBox
        if (overrideClampedHitbox != null) overrideClampedHitbox.box =
            clampedCollisionHitbox.move(entity.position()).move(-1.0, 0.0, -1.0)
    }

    fun setNextDamagedPart(part: String?) {
        nextDamagedPart = part
    }
    
    private val disableHitboxesForCompatibility: Boolean = FabricLoader.getInstance().isModLoaded("bettercombat")

    override fun shouldDamage(actor: LivingEntity, damageSource: DamageSource, amount: Float): Boolean {
        val part = nextDamagedPart
        nextDamagedPart = null

        if (disableHitboxesForCompatibility) return true
        
        if (part == eyeBox || damageSource.`is`(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true

        if (damageSource.`is`(DamageTypeTags.IS_EXPLOSION)) {
            val pos = damageSource.sourcePosition
            if (pos != null) {
                val explosionDirection = MathUtils.unNormedDirection(pos, entity.eyePos())
                if (!MathUtils.facingSameDirection(explosionDirection, entity.lookAngle)) {
                    return true
                }
            }
        }

        if (!damageSource.`is`(DamageTypeTags.IS_PROJECTILE)) {
            val entity: Entity? = damageSource.directEntity
            if (entity is LivingEntity) {
                entity.knockback(0.5, actor.x - entity.getX(), actor.z - entity.getZ(), entity.damageSources().generic(), 0f)
            }
        }

        if (!damageSource.`is`(DamageTypeTags.IS_FIRE)) {
            entity.playSound(Mod.sounds.gauntletClink, 1.0f, actor.random.randomPitch())
        }

        return false
    }
}