package io.github.stuff_stuffs.multipart_entities.common.network;

import io.github.stuff_stuffs.multipart_entities.client.network.PlayerInteractMultipartEntity;
import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartAwareEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;

public class PlayerInteractMultipartReceiver {

    private static void receive(final MinecraftServer minecraftServer, final ServerPlayer serverPlayerEntity, final InteractPacket interactPacket) {
        final int entityId = interactPacket.id();
        final String part = interactPacket.part();
        final PlayerInteractMultipartEntity.InteractionType interactionType = interactPacket.interactionType();
        final InteractionHand hand = interactPacket.hand();
        final boolean isSneaking = interactPacket.isSneaking();
        minecraftServer.execute(() -> {
            serverPlayerEntity.setShiftKeyDown(isSneaking);
            final ServerLevel world = serverPlayerEntity.level();
            final Entity entity = world.getEntity(entityId);
            assert entity != null;
            if (interactionType == PlayerInteractMultipartEntity.InteractionType.INTERACT) {
                entity.interact(serverPlayerEntity, hand, entity.position());
            } else if (interactionType == PlayerInteractMultipartEntity.InteractionType.ATTACK) {
                if (entity instanceof MultipartAwareEntity) {
                    ((MultipartAwareEntity) entity).setNextDamagedPart(part);
                }
                serverPlayerEntity.attack(entity);
            } else {
                throw new RuntimeException();
            }
        });
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(InteractPacket.PACKET_ID, InteractPacket.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(InteractPacket.PACKET_ID, (payload, context) -> {
            PlayerInteractMultipartReceiver.receive(context.server(), context.player(), payload);
        });
    }
}
