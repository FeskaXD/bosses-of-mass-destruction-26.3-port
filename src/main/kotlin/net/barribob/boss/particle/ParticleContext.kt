package net.barribob.boss.particle

import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.phys.Vec3

data class ParticleContext (val spriteProvider: SpriteSet, val world: ClientLevel, val pos: Vec3, val vel: Vec3, val cycleSprites: Boolean = true)