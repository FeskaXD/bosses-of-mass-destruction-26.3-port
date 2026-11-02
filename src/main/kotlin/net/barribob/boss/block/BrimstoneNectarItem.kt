package net.barribob.boss.block

import net.minecraft.world.level.block.state.BlockBehaviour
import net.barribob.boss.Mod
import net.barribob.boss.block.structure_repair.StructureRepair
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.Vec3dNetworkHandler.Companion.sendVec3dPacket
import net.barribob.boss.utils.VecId
import net.barribob.maelstrom.general.event.TimedEvent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

class BrimstoneNectarItem(settings: Item.Properties, private val structureRepairs: List<StructureRepair>) : Item(settings), BlockTooltip {
    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        display: net.minecraft.world.item.component.TooltipDisplay,
        tooltip: java.util.function.Consumer<Component>,
        type: TooltipFlag
    ) {
        tooltip.accept(Component.translatable("item.bosses_of_mass_destruction.brimstone_nectar.tooltip").withStyle(ChatFormatting.DARK_GRAY))
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResult {
        val itemStack = user.getItemInHand(hand)
        if (!world.isClientSide && world is ServerLevel) {
            val usePos = user.blockPosition()
            val structuresToRepair = findStructuresToRepair(world, usePos)

            if(structuresToRepair.isNotEmpty()) {
                scheduleStructureRepair(world, structuresToRepair, usePos)
                playSound(world, user)
                user.cooldowns.addCooldown(user.getItemInHand(hand), 80)
                world.sendVec3dPacket(user.position(), VecId.BrimstoneParticleEffect)

                if (!user.abilities.instabuild) {
                    itemStack.shrink(1)
                }

                user.awardStat(Stats.ITEM_USED.get(this))
                return InteractionResult.SUCCESS
            }
        }

        return InteractionResult.PASS
    }

    private fun findStructuresToRepair(
        world: ServerLevel,
        usePos: BlockPos
    ) = structureRepairs
        .filter {
            val structureStart = getStructureStart(world, usePos, it)
            structureStart.isValid && it.shouldRepairStructure(world, structureStart)
        }

    private fun scheduleStructureRepair(
        world: ServerLevel,
        structuresToRepair: List<StructureRepair>,
        usePos: BlockPos
    ) {
        ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
            structuresToRepair.forEach { it.repairStructure(world, getStructureStart(world, usePos, it)) }
        }, 30))
    }

    private fun getStructureStart(
        world: ServerLevel,
        blockPos: BlockPos,
        it: StructureRepair
    ) = world.structureManager().getStructureAt(blockPos, world.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValueOrThrow(it.associatedStructure()))

    private fun playSound(world: Level, user: Player) {
        world.playSound(
            null as Player?,
            user.x,
            user.y,
            user.z,
            Mod.sounds.brimstone,
            SoundSource.NEUTRAL,
            1.0f,
            world.getRandom().randomPitch()
        )
    }
}