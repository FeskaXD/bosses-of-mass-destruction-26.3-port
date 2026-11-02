package net.barribob.boss.cardinalComponents

import net.barribob.maelstrom.general.event.EventScheduler
import net.minecraft.world.level.Level

interface IWorldEventScheduler {
    fun getWorldEventScheduler(world: Level): EventScheduler
}