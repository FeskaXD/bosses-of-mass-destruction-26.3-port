package net.barribob.boss.mixin;

import net.barribob.boss.block.ObsidilithSummonBlock;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderEyeItem.class)
public abstract class EnderEyeUseMixin {
    @Inject(method="useOn", at = @At("HEAD"), cancellable = true)
    private void onEyeUsed(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir){
        ObsidilithSummonBlock.Companion.onEnderEyeUsed(context, cir);
    }
}
