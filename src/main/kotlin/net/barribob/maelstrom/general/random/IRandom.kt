package net.barribob.maelstrom.general.random

import net.minecraft.world.phys.Vec3

interface IRandom {
    fun getDouble() : Double
    fun getVector() : Vec3
}