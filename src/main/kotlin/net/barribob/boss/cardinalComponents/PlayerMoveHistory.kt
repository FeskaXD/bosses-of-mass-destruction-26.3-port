package net.barribob.boss.cardinalComponents

import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.level.storage.ValueInput
import net.barribob.maelstrom.general.data.HistoricalData
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.HolderLookup
import net.minecraft.world.phys.Vec3

class PlayerMoveHistory(val player: Player) : IPlayerMoveHistoryComponent {
    private val positionalHistory = HistoricalData<Vec3>(Vec3.ZERO, 10)

    override fun serverTick() {
        val previousPosition = positionalHistory.get()
        val newPosition = player.position()

        // Extremely fast movement in one tick is a sign of teleportation or dimension hopping, and thus we should clear history to avoid undefined behavior
        if (previousPosition.distanceToSqr(newPosition) > 5) {
            positionalHistory.clear()
        }

        positionalHistory.set(newPosition)
    }

    override fun getHistoricalPositions(): List<Vec3> = positionalHistory.getAll()
    override fun readData(input: ValueInput) {
    }

    override fun writeData(output: ValueOutput) {
    }
}