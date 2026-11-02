package net.barribob.boss.item

import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting

class CrystalFruitItem(settings: Item.Properties) : Item(settings) {
    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        display: net.minecraft.world.item.component.TooltipDisplay,
        tooltip: java.util.function.Consumer<Component>,
        type: TooltipFlag
    ) {
        tooltip.accept(Component.translatable("item.bosses_of_mass_destruction.crystal_fruit.tooltip").withStyle(ChatFormatting.DARK_GRAY))
    }
}