package net.barribob.boss.cardinalComponents

import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.level.storage.ValueInput
import net.barribob.maelstrom.general.event.EventScheduler
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.HolderLookup
import net.minecraft.world.level.Level

class WorldEventScheduler(val world: Level): IWorldEventSchedulerComponent {
    private val eventScheduler = EventScheduler()

    override fun get(): EventScheduler = eventScheduler
    override fun readData(input: ValueInput) {
    }

    override fun writeData(output: ValueOutput) {
    }

    override fun serverTick() {
        eventScheduler.updateEvents()
    }

    // No persistence for world event scheduler
    override fun clientTick() {
        eventScheduler.updateEvents()
    }
}