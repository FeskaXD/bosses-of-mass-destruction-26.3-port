package net.barribob.boss.utils

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.phys.Vec3

interface Vec3dReceiver {
    fun clientHandler(world: ClientLevel, vec3d: Vec3)
}