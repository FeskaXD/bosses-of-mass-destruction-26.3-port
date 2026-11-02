package net.barribob.boss.mob.mobs.void_blossom.hitbox

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import net.barribob.boss.mob.utils.BaseEntity
import net.minecraft.network.syncher.EntityDataAccessor

class NetworkedHitboxManager(private val entity: BaseEntity, private val hitboxMap: Map<Byte, ICompoundHitbox>, val hitbox: EntityDataAccessor<Byte>) :
    ICompoundHitbox {
    override fun updatePosition() {
        for (hitbox in hitboxMap.values) {
            hitbox.updatePosition()
        }
    }

    override fun getBounds(): EntityBounds = hitboxMap[entity.entityData[hitbox]]!!.getBounds()

    override fun setNextDamagedPart(part: String?) {
        hitboxMap[entity.entityData[hitbox]]!!.setNextDamagedPart(part)
    }
}