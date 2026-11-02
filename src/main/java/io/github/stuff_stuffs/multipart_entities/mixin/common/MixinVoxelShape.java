package io.github.stuff_stuffs.multipart_entities.mixin.common;

import io.github.stuff_stuffs.multipart_entities.common.util.CompoundOrientedBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VoxelShape.class)
public class MixinVoxelShape {
    @Inject(method = "collide(Lnet/minecraft/core/Direction$Axis;Lnet/minecraft/world/phys/AABB;D)D", at = @At("HEAD"), cancellable = true)
    private void hook(final Direction.Axis axis, final AABB box, final double maxDist, final CallbackInfoReturnable<Double> cir) {
        if (box instanceof CompoundOrientedBox) {
            cir.setReturnValue(((CompoundOrientedBox) box).calculateMaxDistance(axis, (VoxelShape) (Object) this, maxDist));
        }
    }
}
