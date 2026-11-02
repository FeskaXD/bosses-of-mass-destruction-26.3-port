package net.barribob.boss.mob.utils

import net.minecraft.world.level.Level

class CompositeEntityTick<T: Level>(vararg tickHandlers: IEntityTick<T>) : IEntityTick<T> {
    private val tickList = tickHandlers.toList()

    override fun tick(world: T) {
        for(tickHandler in tickList) {
            tickHandler.tick(world)
        }
    }
}