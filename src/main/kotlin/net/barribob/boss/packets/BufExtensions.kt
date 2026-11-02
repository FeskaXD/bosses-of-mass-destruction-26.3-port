package net.barribob.boss.packets

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec3

// 26.3 removed FriendlyByteBuf.writeVec3/readVec3; same wire format (three doubles)
fun FriendlyByteBuf.writeVec3(vec: Vec3) {
    writeDouble(vec.x)
    writeDouble(vec.y)
    writeDouble(vec.z)
}

fun FriendlyByteBuf.readVec3(): Vec3 = Vec3(readDouble(), readDouble(), readDouble())
