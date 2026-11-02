package net.barribob.boss.mob.mobs.void_blossom.hitbox

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomEntity
import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.tags.DamageTypeTags
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.AABB

class VoidBlossomCompoundHitbox(
    private val entity: VoidBlossomEntity,
    private val hitboxes: EntityBounds,
    private val root: String,
    private val collisionHitbox: AABB,
    private val spikedBoxes: List<String>
) : ICompoundHitbox, IDamageHandler, IEntityTick<ServerLevel> {
    private var nextDamagedPart: String? = null

    override fun updatePosition() {
        val rootYaw = hitboxes.root(root)
        rootYaw.setRotation(0.0, -entity.yRot.toDouble(), 0.0, true)

        rootYaw.setX(entity.x)
        rootYaw.setY(entity.y)
        rootYaw.setZ(entity.z)

        val overrideBox = hitboxes.overrideBox
        if (overrideBox != null) overrideBox.box = collisionHitbox.move(entity.position()).move(-1.0, 0.0, -1.0)
    }

    override fun getBounds(): EntityBounds = hitboxes

    override fun setNextDamagedPart(part: String?) {
        nextDamagedPart = part
    }

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        val part = nextDamagedPart
        nextDamagedPart = null

        if (result) {
            if (spikedBoxes.contains(part) && !damageSource.`is`(DamageTypeTags.IS_PROJECTILE)) {
                val damage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE).toFloat()
                damageSource.entity?.hurt(entity.level().damageSources().thorns(entity), damage)
            }
        }
    }

    override fun tick(world: ServerLevel) {
        val rootYaw = hitboxes.root(root)
        rootYaw.setRotation(0.0, -entity.yRot.toDouble(), 0.0, true)
    }
}