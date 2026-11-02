package net.barribob.boss.cardinalComponents

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

interface IPlayerMoveHistory {
    fun getPlayerPositions(serverPlayerEntity: ServerPlayer): List<Vec3>
}