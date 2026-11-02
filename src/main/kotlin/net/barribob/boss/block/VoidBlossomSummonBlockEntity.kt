package net.barribob.boss.block

import net.minecraft.world.entity.EntitySpawnReason
import net.barribob.boss.mob.Entities
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class VoidBlossomSummonBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : BlockEntity(type, pos, state) {
    private var age = 0

    companion object {
        fun tick(world: Level, pos: BlockPos, state: BlockState, entity: VoidBlossomSummonBlockEntity) {
            if(!world.isClientSide && entity.age % 20 == 0) {
                val playersInBox = world.getEntitiesOfClass(Player::class.java, AABB(pos).inflate(40.0)).any()
                if (playersInBox) {
                    val spawnPos = pos.asVec3d().add(Vec3(0.5, 0.0, 0.5))
                    val boss = Entities.VOID_BLOSSOM.create(world, EntitySpawnReason.TRIGGERED)
                    boss!!.absSnapTo(spawnPos.x, spawnPos.y, spawnPos.z)
                    world.addFreshEntity(boss)
                    world.destroyBlock(pos, false)
                }
            }
            entity.age++
        }
    }
}