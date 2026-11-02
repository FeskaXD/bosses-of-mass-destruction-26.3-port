package net.barribob.boss.cardinalComponents

import net.minecraft.world.phys.Vec3
import org.ladysnake.cca.api.v3.component.ComponentV3
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent

interface IPlayerMoveHistoryComponent: ComponentV3, ServerTickingComponent {
    fun getHistoricalPositions(): List<Vec3>
}