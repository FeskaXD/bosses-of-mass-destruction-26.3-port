package net.barribob.maelstrom.static_utilities

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec3

fun FriendlyByteBuf.readFloatList(count: Int): List<Float> {
    if (count < 0) throw IllegalArgumentException("Count should be greater than zero")
    return (0 until count).map { this.readFloat() }
}

fun FriendlyByteBuf.writeFloatList(list: List<Float>) = list.forEach { this.writeFloat(it) }

fun FriendlyByteBuf.writeVec3d(vec: Vec3) = this.writeFloatList(
    listOf(vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat()))

fun FriendlyByteBuf.readVec3d(): Vec3 {
    val floatList = readFloatList(3)
    return Vec3(floatList[0].toDouble(), floatList[1].toDouble(), floatList[2].toDouble())
}