package net.barribob.maelstrom.static_utilities

import net.minecraft.world.entity.Entity
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.phys.Vec3

fun Entity.addVelocity(vec: Vec3) {
    this.push(vec.x, vec.y, vec.z)
}

fun Entity.setPos(vec: Vec3) {
    this.absSnapTo(vec.x, vec.y, vec.z)
}

fun Entity.eyePos(): Vec3 = this.getEyePosition(1f)

val Entity.lastRenderPos: Vec3
    get() = Vec3(this.xOld, this.yOld, this.zOld)

val Entity.registryId: String
    get() = BuiltInRegistries.ENTITY_TYPE.getId(type).toString()