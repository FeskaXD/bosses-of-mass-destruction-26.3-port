package net.barribob.boss.block

import me.shedaniel.autoconfig.AutoConfig
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.config.ModConfig
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import com.geckolib.animatable.GeoBlockEntity
import com.geckolib.animatable.instance.AnimatableInstanceCache
import com.geckolib.animatable.manager.AnimatableManager
import com.geckolib.animation.AnimationController
import com.geckolib.animation.RawAnimation
import com.geckolib.util.GeckoLibUtil


class LevitationBlockEntity(
    block: Block, type: BlockEntityType<*>,
    pos: BlockPos, state: BlockState
) : ChunkCacheBlockEntity(block, type, pos, state), GeoBlockEntity {
    override fun registerControllers(data: AnimatableManager.ControllerRegistrar) {
        data.add(
            AnimationController<LevitationBlockEntity> { it.setAndContinue(rotate) }
        )
    }

    var animationAge = 0
    private val animationFactory = GeckoLibUtil.createInstanceCache(this)
    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = animationFactory

    companion object {
        val rotate: RawAnimation = RawAnimation.begin().thenLoop("rotate")
        private val flight = HashSet<ServerPlayer>()
        private val particlesFactory = ClientParticleBuilder(Particles.LINE)
            .color(ModColors.COMET_BLUE)
            .brightness(Particles.FULL_BRIGHT)
            .colorVariation(0.5)
            .scale(0.075f)
        private val tableOfElevationRadius = AutoConfig.getConfigHolder(ModConfig::class.java).config.generalConfig.tableOfElevationRadius.toDouble()
        
        fun tick(world: Level, pos: BlockPos, state: BlockState, entity: LevitationBlockEntity) {
            ChunkCacheBlockEntity.tick(world, pos, state, entity)
            if (world.isClientSide) {
                entity.animationAge++
                val box = getAffectingBox(world, pos.asVec3d())
                val playersInBox = world.getEntitiesOfClass(Player::class.java, box)
                for (player in playersInBox) {
                    for (x in listOf(box.minX, box.maxX)) {
                        val zRand = box.center.z + box.zsize * RandomUtils.double(0.5)
                        particlesFactory.build(randYPos(x, player, zRand))
                    }

                    for (z in listOf(box.minZ, box.maxZ)) {
                        val xRand = box.center.x + box.xsize * RandomUtils.double(0.5)
                        particlesFactory.build(randYPos(xRand, player, z))
                    }
                }
            }
        }

        private fun randYPos(x: Double, player: Player, z: Double) =
            Vec3(x, player.y + RandomUtils.double(0.5) + 1, z)

        fun tickFlight(player: ServerPlayer) {
            val blockToCheck = mutableListOf<BlockPos>()
            for (x in -1..1) {
                for (z in -1..1) {
                    blockToCheck.add(BlockPos(x * tableOfElevationRadius.toInt(), 0, z * tableOfElevationRadius.toInt()))
                }
            }
            val chunksToCheck = blockToCheck.map { ChunkPos.containing(it.offset(player.blockPosition())) }.toSet()
            val hasLevitationBlock = chunksToCheck.any { it ->
                val blockCache = ModComponents.getChunkBlockCache(player.level())

                if (blockCache.isPresent) {
                    val blocks = blockCache.get().getBlocksFromChunk(it, ModBlocks.levitationBlock)
                    blocks.any { getAffectingBox(player.level(), it.asVec3d()).contains(player.position()) }
                } else {
                    false
                }
            }

            /**
             * Known bugs:
             * - does not have persistence (e.g. loading why flying will fall next time it is opened, though without fall damage)
             * - does not handle changing gamemodes that change these abilities (e.g. changing to survival or losing the flying ability
             * from taking another modded item off will not work as expected.)
             * - However, it will be more-or-less able to work with other mods that mess with the flight ability provided that
             * they do not set it every tick
             */
            if (!hasLevitationBlock) {
                if (flight.contains(player)) {
                    if (!player.isCreative && !player.isSpectator) {
                        player.abilities.mayfly = false
                        player.abilities.flying = false
                        player.connection.send(ClientboundPlayerAbilitiesPacket(player.abilities))
                    }
                    flight.remove(player)
                }
            } else if (!flight.contains(player)) {
                flight.add(player)
                if (!player.abilities.mayfly) {
                    player.abilities.mayfly = true
                    player.connection.send(ClientboundPlayerAbilitiesPacket(player.abilities))
                }
            }
        }

        private fun getAffectingBox(world: Level, pos: Vec3) : AABB {
            return AABB(pos.x, world.minY.toDouble(), pos.z, (pos.x + 1), world.height.toDouble(), (pos.z + 1))
                .inflate(tableOfElevationRadius, 0.0, tableOfElevationRadius)
        }
    }
}