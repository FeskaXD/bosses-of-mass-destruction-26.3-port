package io.github.stuff_stuffs.multipart_entities.mixin.common;

import io.github.stuff_stuffs.multipart_entities.common.util.CompoundOrientedBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Shapes.class)
public class MixinVoxelShapes {
    @Inject(method = "create(Lnet/minecraft/world/phys/AABB;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private static void hook(final AABB box, final CallbackInfoReturnable<VoxelShape> cir) {
        if (box instanceof CompoundOrientedBox) {
            cir.setReturnValue(((CompoundOrientedBox) box).toVoxelShape());
        }
    }
}
