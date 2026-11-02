package net.barribob.boss.mixin;

import net.barribob.boss.block.MonolithBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ExplosionDamageCalculator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerLevel.class)
public abstract class ExplosionMixin {
    @ModifyVariable(at = @At(value = "HEAD"), method = "explode", argsOnly = true)
    private float Explosion(float g, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionBehavior, double d, double e, double f) {
        Level world = (Level) (Object) this;
        if (!world.isClientSide()) {
            return MonolithBlock.Companion.getExplosionPower(world, BlockPos.containing(d, e, f), g);
        }

        return g;
    }
}
