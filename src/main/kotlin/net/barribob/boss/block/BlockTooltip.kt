package net.barribob.boss.block

import net.minecraft.network.chat.Component
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.block.Block
import java.util.function.Consumer

/** 26.3 removed Block.appendHoverText; blocks implementing this get their tooltip through [TooltipBlockItem]. */
interface BlockTooltip {
    fun appendHoverText(stack: ItemStack, context: Item.TooltipContext, display: TooltipDisplay, tooltip: Consumer<Component>, flag: TooltipFlag)
}

class TooltipBlockItem(block: Block, properties: Properties) : BlockItem(block, properties) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, display: TooltipDisplay, tooltip: Consumer<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, context, display, tooltip, flag)
        (block as? BlockTooltip)?.appendHoverText(stack, context, display, tooltip, flag)
    }
}
