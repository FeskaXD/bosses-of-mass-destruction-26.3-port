package io.github.stuff_stuffs.multipart_entities.mixin.client;

import io.github.stuff_stuffs.multipart_entities.client.network.PlayerInteractMultipartEntity;
import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartAwareEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinClientPlayerInteractionManager {
    @Shadow
    private void ensureHasSentCarriedItem() {
        throw new AssertionError();
    }

    @Shadow
    private GameType localPlayerMode;

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attackHook(final Player player, final Entity target, final CallbackInfo ci) {
        if (target instanceof MultipartAwareEntity) {
            ensureHasSentCarriedItem();
            PlayerInteractMultipartEntity.attack(Minecraft.getInstance(), InteractionHand.MAIN_HAND, (MultipartAwareEntity) target);
            if (localPlayerMode != GameType.SPECTATOR) {
                final Minecraft client = Minecraft.getInstance();
                final Vec3 pos = client.getCameraEntity().getEyePosition(client.getDeltaTracker().getGameTimeDeltaPartialTick(true));
                final Vec3 dir = client.getCameraEntity().getViewVector(client.getDeltaTracker().getGameTimeDeltaPartialTick(true));
                final double reach = client.player.entityInteractionRange();
                ((MultipartAwareEntity) target).setNextDamagedPart(((MultipartAwareEntity) target).getBounds().raycast(pos, pos.add(dir.scale(reach))));
                player.attack(target);
                player.resetAttackStrengthTicker();
            }
            ci.cancel();
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interactHook(final Player player, final Entity entity, final net.minecraft.world.phys.EntityHitResult hitResult, final InteractionHand hand, final CallbackInfoReturnable<InteractionResult> cir) {
        if (entity instanceof MultipartAwareEntity) {
            ensureHasSentCarriedItem();
            cir.setReturnValue(PlayerInteractMultipartEntity.interact(Minecraft.getInstance(), hand, (MultipartAwareEntity) entity));
        }
    }
}
