package net.barribob.boss.item

import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.stats.Stats
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.Level

class ChargedEnderPearlItem(settings: Item.Properties) : Item(settings) {
    val itemCooldown = 180

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResult {
        val itemStack = user.getItemInHand(hand)
        world.playSound(
            null as Player?,
            user.x,
            user.y,
            user.z,
            SoundEvents.ENDER_PEARL_THROW,
            SoundSource.NEUTRAL,
            0.5f,
            0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f)
        )

        user.cooldowns.addCooldown(user.getItemInHand(hand), itemCooldown)

        if (!world.isClientSide) {
            val projectile = ChargedEnderPearlEntity(world, user)
            projectile.setItem(itemStack)
            projectile.shootFromRotation(user, user.xRot, user.yRot, 0.0f, 1.5f, 1.0f)
            world.addFreshEntity(projectile)
        }
        user.awardStat(Stats.ITEM_USED.get(this))
        return InteractionResult.SUCCESS
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        display: net.minecraft.world.item.component.TooltipDisplay,
        tooltip: java.util.function.Consumer<Component>,
        type: TooltipFlag
    ) {
        tooltip.accept(Component.translatable("item.bosses_of_mass_destruction.charged_ender_pearl.tooltip").withStyle(ChatFormatting.DARK_GRAY))
        tooltip.accept(Component.translatable("item.bosses_of_mass_destruction.charged_ender_pearl.tooltip2").withStyle(ChatFormatting.DARK_GRAY))
    }
}