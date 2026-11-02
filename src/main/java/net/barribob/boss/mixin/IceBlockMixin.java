package net.barribob.boss.mixin;

import net.barribob.boss.Mod;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceBlock.class)
public abstract class IceBlockMixin {
    @Inject(method = "playerDestroy", at = @At("TAIL"))
    private void onAfterBreak(ServerLevel world, net.minecraft.server.level.ServerPlayer player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack, CallbackInfo ci) {
        var registry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        if (EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(Enchantments.SILK_TOUCH), stack) == 0
                && world instanceof ServerLevel serverWorld
                && serverWorld.structureManager().getStructureAt(pos, serverWorld.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValueOrThrow(Mod.INSTANCE.getStructures().getLichStructureRegistry().getConfiguredStructureKey())).isValid()) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
