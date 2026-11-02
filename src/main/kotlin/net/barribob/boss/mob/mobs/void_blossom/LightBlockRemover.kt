package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.Mod
import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LightBlock
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import kotlin.math.round

class LightBlockRemover(private val entity: LivingEntity) : IEntityTick<ServerLevel> {
    override fun tick(world: ServerLevel) {
        ++entity.deathTime
        val interceptedTime = MathUtils.ratioLerp(entity.deathTime.toFloat(), 0.5f, deathMaxAge, 0f) * 0.7f
        world.setBlock(entity.blockPosition(), Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, round((1 - interceptedTime) * 15).toInt()), 3)

        if(entity.deathTime == 49) {
            world.playSound(entity.position(), Mod.sounds.voidBlossomFall, SoundSource.HOSTILE, 1.5f, range = 32.0)
        }

        if (entity.deathTime == deathMaxAge.toInt()) {
            if (world.getBlockState(entity.blockPosition()).block == Blocks.LIGHT) {
                world.setBlock(entity.blockPosition(), Blocks.AIR.defaultBlockState(), 3)
            }
            world.broadcastEntityEvent(entity, 60.toByte())
            entity.remove(Entity.RemovalReason.KILLED)
        }
    }

    companion object {
        const val deathMaxAge = 70f
    }
}