package net.barribob.boss.item

import net.barribob.boss.Mod
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemUseAnimation
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

class EarthdiveSpear(settings: Item.Properties) : Item(settings) {
    companion object {
        fun createAttributeModifiers(): ItemAttributeModifiers {
            return ItemAttributeModifiers.builder().add(
                Attributes.ATTACK_DAMAGE,
                AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8.0, AttributeModifier.Operation.ADD_VALUE), 
                EquipmentSlotGroup.MAINHAND
            ).add(
                Attributes.ATTACK_SPEED,
                AttributeModifier(BASE_ATTACK_SPEED_ID, -2.9000000953674316, AttributeModifier.Operation.ADD_VALUE), 
                EquipmentSlotGroup.MAINHAND
            ).build()
        }
    }   

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        display: net.minecraft.world.item.component.TooltipDisplay,
        tooltip: java.util.function.Consumer<Component>,
        type: TooltipFlag
    ) {
        tooltip.accept(Component.translatable("item.bosses_of_mass_destruction.earthdive_spear.tooltip").withStyle(ChatFormatting.DARK_GRAY))
    }

    override fun releaseUsing(stack: ItemStack, world: Level, user: LivingEntity, remainingUseTicks: Int): Boolean {
        if (user is Player) {
            if (isCharged(stack, remainingUseTicks)) {
                if (!world.isClientSide && world is ServerLevel) {
                    if (WallTeleport(world, user).tryTeleport(user.lookAngle, user.eyePosition) ||
                        WallTeleport(world, user).tryTeleport(user.lookAngle, user.eyePosition.add(0.0, -1.0, 0.0))
                    ) {
                        stack.hurtAndBreak(1, user, EquipmentSlot.MAINHAND)
                        user.awardStat(Stats.ITEM_USED.get(this))
                        world.playSound(
                            null as Player?,
                            user,
                            Mod.sounds.earthdiveSpearThrow,
                            SoundSource.PLAYERS,
                            1.0f,
                            user.random.randomPitch()
                        )
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun postHurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND)
    }

    override fun mineBlock(
        stack: ItemStack,
        world: Level,
        state: BlockState,
        pos: BlockPos,
        miner: LivingEntity
    ): Boolean {
        if (state.getDestroySpeed(world, pos).toDouble() != 0.0) {
            stack.hurtAndBreak(2, miner, EquipmentSlot.MAINHAND)
        }
        return true
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResult {
        val itemStack = user.getItemInHand(hand)
        return if (itemStack.damageValue >= itemStack.maxDamage - 1) {
            InteractionResult.FAIL
        } else {
            user.startUsingItem(hand)
            InteractionResult.CONSUME
        }
    }

    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        super.onUseTick(world, user, stack, remainingUseTicks)
        if (world is ServerLevel) {
            if (isCharged(stack, remainingUseTicks)) {
                val teleportAction: (BlockPos) -> Unit = { spawnTeleportParticles(world, user) }
                val wallTeleport = WallTeleport(world, user)
                if (!wallTeleport.tryTeleport(user.lookAngle, user.eyePosition, teleportAction)) {
                    wallTeleport.tryTeleport(user.lookAngle, user.eyePosition.add(0.0, -1.0, 0.0), teleportAction)
                }
            }
        }
    }

    private fun spawnTeleportParticles(world: ServerLevel, user: LivingEntity) {
        val pos = user.eyePosition.add(user.lookAngle.scale(0.15)).add(RandomUtils.randVec())
        val vel = user.eyePosition.add(user.lookAngle.scale(4.0)).subtract(pos)
        world.spawnParticle(Particles.EARTHDIVE_INDICATOR, pos, vel, speed = 0.07)
    }

    private fun isCharged(stack: ItemStack, remainingUseTicks: Int): Boolean = 72000 - remainingUseTicks >= 10
    override fun getUseDuration(stack: ItemStack, user: LivingEntity): Int = 72000
    override fun getUseAnimation(stack: ItemStack) = ItemUseAnimation.SPEAR
}