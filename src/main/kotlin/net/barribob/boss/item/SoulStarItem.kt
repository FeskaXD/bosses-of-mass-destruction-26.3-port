package net.barribob.boss.item

import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.InteractionResult
import net.barribob.boss.Mod
import net.barribob.boss.block.ChiseledStoneAltarBlock
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.spawn.*
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.EnderEyeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.stats.Stats
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting; import net.minecraft.world.InteractionHand; import net.minecraft.world.level.block.Rotation
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level

class SoulStarItem(settings: Item.Properties) : Item(settings) {
    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        display: net.minecraft.world.item.component.TooltipDisplay,
        tooltip: java.util.function.Consumer<Component>,
        type: TooltipFlag
    ) {
        tooltip.accept(Component.translatable("item.bosses_of_mass_destruction.soul_star.tooltip").withStyle(ChatFormatting.DARK_GRAY))
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val world = context.level
        val blockPos = context.clickedPos
        val blockState = world.getBlockState(blockPos)
        if (blockState.`is`(ModBlocks.chiseledStoneAltar) && !blockState.getValue(ChiseledStoneAltarBlock.lit)) {
            return if (world.isClientSide) {
                clientSoulStartPlace(blockPos)
                InteractionResult.SUCCESS
            } else {
                serverSoulStarPlace(blockState, world, blockPos, context)
                world.playSound(
                    null as Player?,
                    context.clickLocation.x,
                    context.clickLocation.y,
                    context.clickLocation.z,
                    Mod.sounds.soulStar,
                    SoundSource.NEUTRAL,
                    0.5f,
                    world.random.randomPitch()
                )
                InteractionResult.PASS
            }
        }

        return InteractionResult.PASS
    }

    private fun serverSoulStarPlace(
        blockState: BlockState,
        world: Level,
        blockPos: BlockPos,
        context: UseOnContext
    ) {
        val blockState2 = blockState.setValue(ChiseledStoneAltarBlock.lit, true)
        world.setBlock(blockPos, blockState2, 2)
        context.itemInHand.shrink(1)
        val quarterAltarPosition = listOf(BlockPos(12, 0, 0), BlockPos(6, 0, 6))
        val allPotentialAltarPositions = Rotation.values().flatMap { rot -> quarterAltarPosition.map { it.rotate(rot) } }
        val numberOfAltarsFilled = allPotentialAltarPositions.count {
            val state = world.getBlockState(blockPos.offset(it))
            state.hasProperty(ChiseledStoneAltarBlock.lit) && state.getValue(ChiseledStoneAltarBlock.lit)
        }
        if (numberOfAltarsFilled == 3) {
            val eventScheduler = ModComponents.getWorldEventScheduler(world)
            eventScheduler.addEvent(TimedEvent({
                allPotentialAltarPositions.forEach {
                    if (world.getBlockState(blockPos.offset(it))
                            .hasProperty(ChiseledStoneAltarBlock.lit)
                    ) world.destroyBlock(blockPos.offset(it), false)
                }

                world.destroyBlock(blockPos, false)

                spawnLich(blockPos, world)
            }, 20))
        }
    }

    @Environment(EnvType.CLIENT)
    private fun clientSoulStartPlace(blockPos: BlockPos) {
        val centralPos = blockPos.asVec3d().add(Vec3(0.5, 1.2, 0.5))
        MathUtils.circleCallback(0.5, 15, VecUtils.yAxis) {
            val particleVel = VecUtils.yAxis.scale(0.03 + net.barribob.maelstrom.static_utilities.RandomUtils.double(0.01))
            val particlePos = centralPos.add(it)
            ChiseledStoneAltarBlock.blueFireParticleFactory.build(particlePos, particleVel)
        }
    }

    /**
     * [EnderEyeItem.use]
     */
    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResult {
        val itemStack = user.getItemInHand(hand)
        val hitResult: HitResult = getPlayerPOVHitResult(world, user, ClipContext.Fluid.NONE)
        return if (hitResult.type == HitResult.Type.BLOCK && world.getBlockState((hitResult as BlockHitResult).blockPos)
                .`is`(ModBlocks.chiseledStoneAltar)
        ) {
            InteractionResult.PASS
        } else {
            user.startUsingItem(hand)
            if (world is ServerLevel) {
                val blockPos = world.findNearestMapStructure(Mod.structures.soulStarStructureKey, user.blockPosition(),  100, false)
                if (blockPos != null) {
                    val entity = SoulStarEntity(world, user.x, user.eyeY, user.z)
                    entity.setItem(itemStack)
                    entity.initTargetPos(blockPos)
                    world.addFreshEntity(entity)
                    world.playSound(
                        null as Player?,
                        user.x,
                        user.y,
                        user.z,
                        SoundEvents.ENDER_EYE_LAUNCH,
                        SoundSource.NEUTRAL,
                        0.5f,
                        0.4f / (world.random.nextFloat() * 0.4f + 0.8f)
                    )
                    if (!user.abilities.instabuild) {
                        itemStack.shrink(1)
                    }
                    user.awardStat(Stats.ITEM_USED.get(this))
                    user.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true)
                    return InteractionResult.SUCCESS
                }
                return InteractionResult.PASS
            }
            InteractionResult.CONSUME
        }
    }

    companion object {
        fun spawnLich(blockPos: BlockPos, world: Level) {
            val compoundTag = CompoundTag()
            compoundTag.putString("id", Mod.identifier("lich").toString())

            val spawnPos = blockPos.asVec3d()
            val spawned = MobPlacementLogic(
                HorizontalRangedSpawnPosition(spawnPos, 15.0, 30.0, ModRandom()),
                CompoundTagEntityProvider(compoundTag, world, Mod.LOGGER),
                MobEntitySpawnPredicate(world),
                SimpleMobSpawner(world as ServerLevel)
            ).tryPlacement(200)

            if (!spawned) {
                val entity = Entities.LICH.create(world, EntitySpawnReason.TRIGGERED)
                if (entity != null) {
                    val defaultSpawnPos = spawnPos.add(VecUtils.xAxis.scale(5.0))
                    entity.syncPacketPositionCodec(defaultSpawnPos.x, defaultSpawnPos.y, defaultSpawnPos.z)
                    entity.absSnapTo(defaultSpawnPos.x, defaultSpawnPos.y, defaultSpawnPos.z)
                    world.addFreshEntity(entity)
                }
            }
        }
    }
}