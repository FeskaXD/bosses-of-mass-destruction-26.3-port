package net.barribob.boss.item

import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.world.entity.Entity
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

class WallTeleport(private val world: ServerLevel, private val entity: Entity) {
    private val startRange = 3.0
    private val endRange = 20.0

    fun tryTeleport(direction: Vec3, position: Vec3, action: (BlockPos) -> Unit = ::teleportTo): Boolean {
        val context = Context(direction, position)
        val teleportStart = getTeleportStart(context)
        if (teleportStart != null) {
            val teleportEnd = getTeleportEnd(context, teleportStart)
            if (teleportEnd != null) {
                action(teleportEnd)
                return true
            }
        }

        return false
    }

    private fun getTeleportStart(context: Context): BlockPos? {
        val startPos = BlockPos.containing(context.position)
        val endPos = BlockPos.containing(context.position.add(context.direction.scale(startRange)))
        val blocksToCheck = MathUtils.getBlocksInLine(startPos, endPos)
        for (pos in blocksToCheck) {
            if (world.getBlockState(pos).isRedstoneConductor(world, pos)) {
                return pos
            }
        }

        return null
    }

    private fun getTeleportEnd(context: Context, startPos: BlockPos): BlockPos? {
        val endPos = startPos.offset(BlockPos.containing(context.direction.scale(endRange)))
        val blocksToCheck = MathUtils.getBlocksInLine(startPos, endPos)
        for (pos in blocksToCheck) {
            val blockState = world.getBlockState(pos)
            if (blockState.isAir && world.getBlockState(pos.above()).isAir) {
                return pos
            }

            if (blockState.block.defaultDestroyTime() < 0) {
                return null
            }
        }

        return null
    }

    private fun teleportTo(teleportPos: BlockPos) {
        val pos = teleportPos.asVec3d().add(Vec3(0.5, 0.0, 0.5))
        entity.teleportTo(pos.x, pos.y, pos.z)
    }

    private data class Context(val direction: Vec3, val position: Vec3)
}