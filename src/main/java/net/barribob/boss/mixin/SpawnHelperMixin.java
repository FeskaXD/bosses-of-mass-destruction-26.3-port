package net.barribob.boss.mixin;

import net.barribob.boss.block.MobWardBlock;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public abstract class SpawnHelperMixin {
    @Inject(
            method = "isValidSpawnPostitionForType(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/level/biome/MobSpawnSettings$SpawnerData;Lnet/minecraft/core/BlockPos$MutableBlockPos;D)Z",
            at = @At("RETURN"),
            cancellable = true)
    private static void canSpawn(ServerLevel world, MobCategory group, StructureManager structureAccessor, ChunkGenerator chunkGenerator, MobSpawnSettings.SpawnerData spawnEntry, BlockPos.MutableBlockPos pos, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        MobWardBlock.Companion.canSpawn(world, pos, cir);
    }
}