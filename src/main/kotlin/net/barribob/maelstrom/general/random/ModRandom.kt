package net.barribob.maelstrom.general.random

import net.barribob.maelstrom.static_utilities.RandomUtils
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

class ModRandom : IRandom {
    override fun getDouble(): Double = Random.nextDouble()
    override fun getVector(): Vec3 = RandomUtils.randVec()
}