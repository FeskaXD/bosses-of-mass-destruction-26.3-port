package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.mobs.gauntlet.ServerGauntletDeathHandler.Companion.deathAnimationTime
import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.Entity

class ClientGauntletDeathHandler(val entity: GauntletEntity): IEntityTick<ClientLevel> {
    override fun tick(world: ClientLevel) {
        ++entity.deathTime
        if (entity.deathTime == deathAnimationTime) {
            entity.remove(Entity.RemovalReason.KILLED)
        }
    }
}