package io.github.stuff_stuffs.multipart_entities.mixin.common;

import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartAwareEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class MixinProjectileEntity extends Entity {

    public MixinProjectileEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "onHit", at = @At("HEAD"))
    private void onHit(HitResult hitResult, CallbackInfo ci) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY && hitResult instanceof EntityHitResult) {
            Entity target = ((EntityHitResult) hitResult).getEntity();
            if (target instanceof MultipartAwareEntity) {
                MultipartAwareEntity multipartAwareEntity = (MultipartAwareEntity) target;
                String nextDamagedPart = multipartAwareEntity.getBounds().raycast(position(), position().add(getDeltaMovement()));
                multipartAwareEntity.setNextDamagedPart(nextDamagedPart);
            }
        }
    }
}
