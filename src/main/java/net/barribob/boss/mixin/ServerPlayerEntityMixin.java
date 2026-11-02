package net.barribob.boss.mixin;

import net.barribob.boss.block.LevitationBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        LevitationBlockEntity.Companion.tickFlight((ServerPlayer) (Object) this);
    }
}
