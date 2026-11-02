package net.barribob.boss.block

import net.minecraft.world.level.ScheduledTickAccess
import net.minecraft.world.level.block.state.BlockBehaviour
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomEntity
import net.barribob.boss.mob.utils.EntityAdapter
import net.barribob.boss.mob.utils.EntityStats
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.NetworkUtils.Companion.sendHealPacket
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.util.RandomSource
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader

class VoidBlossomBlock(settings: BlockBehaviour.Properties) : Block(settings) {
    private val shape = box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0)
    private val spikeParticleFactory = ClientParticleBuilder(Particles.LINE)
        .color { MathUtils.lerpVec(it, ModColors.VOID_PURPLE, ModColors.ULTRA_DARK_PURPLE) }
        .colorVariation(0.15)
        .brightness(Particles.FULL_BRIGHT)
        .scale(0.25f)
        .age(10, 15)

    private fun healNearbyEntities(world: ServerLevel, pos: BlockPos) {
        world.getEntities(Entities.VOID_BLOSSOM, AABB(pos).inflate(40.0, 20.0, 40.0)) { true }.forEach {
            ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
                LichUtils.cappedHeal(EntityAdapter(it), EntityStats(it), VoidBlossomEntity.hpMilestones, 10f, it::heal)
            }, healAnimationDelay))
            it.sendHealPacket(pos.asVec3d().add(VecUtils.unit.scale(0.5)), it.position().add(VecUtils.yAxis.scale(5.0)))
        }
    }

    override fun tick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        world.scheduleTick(pos, this, healDelay)
        healNearbyEntities(world, pos)
    }

    override fun onPlace(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        oldState: BlockState,
        notify: Boolean
    ) {
        world.scheduleTick(pos, this, 1)
    }

    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        return canSupportCenter(world, pos.below(), Direction.UP) && !world.isWaterAt(pos)
    }

    override fun updateShape(
        state: BlockState,
        world: LevelReader,
        scheduledTickAccess: ScheduledTickAccess,
        pos: BlockPos,
        direction: Direction,
        neighborPos: BlockPos,
        neighborState: BlockState,
        random: RandomSource
    ): BlockState {
        return if (direction == Direction.DOWN && !canSurvive(state, world, pos)) {
            destroy(world as LevelAccessor, pos, state)
            Blocks.AIR.defaultBlockState()
        } else {
            super.updateShape(state, world, scheduledTickAccess, pos, direction, neighborPos, neighborState, random)
        }
    }

    override fun destroy(world: LevelAccessor, pos: BlockPos, state: BlockState) {
        for(x in -1..1) {
            for (z in -1..1) {
                for(y in -1..1) {
                    if((x != 0 || z != 0)) {
                        val pos1 = pos.offset(x, y, z)
                        if(world.getBlockState(pos1).block == ModBlocks.vineWall) {
                            world.scheduleTick(pos1, ModBlocks.vineWall, (2 - y) * 20 + RandomUtils.range(0, 19))
                        }
                    }
                }
            }
        }
    }

    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape = shape

    override fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (world.isClientSide) {
            for (i in 0 until 12) {
                val vel = VecUtils.yAxis.scale(RandomUtils.range(0.1, 0.2))
                val spawnPos =
                    pos.asVec3d().add(VecUtils.unit.scale(0.5)).add(RandomUtils.randVec().planeProject(VecUtils.yAxis).normalize().scale(0.5))
                spikeParticleFactory.build(spawnPos, vel)
            }
        }
        return super.playerWillDestroy(world, pos, state, player)
    }

    companion object {
        private const val healAnimationDelay = 16
        private const val healDelay = 64
        private val healParticleFactory = ClientParticleBuilder(Particles.OBSIDILITH_BURST)
            .color { MathUtils.lerpVec(it, ModColors.PINK, ModColors.ULTRA_DARK_PURPLE) }
            .colorVariation(0.15)
            .brightness(Particles.FULL_BRIGHT)
            .scale { 0.4f * (1 - it * 0.75f) }
            .age(10)

        private val petalParticleFactory = ClientParticleBuilder(Particles.PETAL)
            .color { MathUtils.lerpVec(it, ModColors.PINK, ModColors.ULTRA_DARK_PURPLE) }
            .brightness(Particles.FULL_BRIGHT)
            .colorVariation(0.15)
            .scale { 0.15f * (1 - it * 0.25f) }
            .age(30)

        @Environment(EnvType.CLIENT)
        fun handleVoidBlossomHeal(world: ClientLevel, source: Vec3, dest: Vec3) {
            spawnHealParticle(dest, source, world)
            spawnChargeParticle(source, world)
        }

        @Environment(EnvType.CLIENT)
        private fun spawnChargeParticle(source: Vec3, world: ClientLevel) {
            val particlePos = source.add(VecUtils.yAxis.scale(0.25))
            ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
                healParticleFactory.build(particlePos.add(RandomUtils.randVec().scale(0.2)))
            }, 32, 32))
        }

        @Environment(EnvType.CLIENT)
        private fun spawnHealParticle(
            dest: Vec3,
            source: Vec3,
            world: ClientLevel
        ) {
            val particlePositions = mutableListOf<Vec3>()
            val numCirclePoints = healAnimationDelay
            val circlePoints = MathUtils.circlePoints(0.5, numCirclePoints, dest.subtract(source).normalize()).toList()
            MathUtils.lineCallback(source, dest, 32) { pos, i ->
                particlePositions.add(pos.add(circlePoints[i % numCirclePoints]))
            }

            var i = 0
            ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
                healParticleFactory.build(particlePositions[i])
                healParticleFactory.build(particlePositions[i + 1])
                i += 2
            }, 0, healAnimationDelay))
        }

        fun handleVoidBlossomPlace(pos: Vec3) {
            for (i in 0..12) {
                val spawnPos = pos.add(RandomUtils.randVec().planeProject(VecUtils.yAxis).normalize().scale(0.5))
                val vel = VecUtils.yAxis.scale(RandomUtils.range(0.1, 0.3))
                val randomRot = RandomUtils.range(0, 360)
                val angularMomentum = RandomUtils.randSign() * 4f

                petalParticleFactory
                    .continuousRotation { randomRot + it.getAge() * angularMomentum }
                    .continuousVelocity { vel.scale(1.0 - it.ageRatio) }
                    .build(spawnPos, vel)
            }
        }
    }
}