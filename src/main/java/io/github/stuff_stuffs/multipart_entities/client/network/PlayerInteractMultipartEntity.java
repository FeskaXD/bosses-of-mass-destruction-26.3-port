package io.github.stuff_stuffs.multipart_entities.client.network;

import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartAwareEntity;
import io.github.stuff_stuffs.multipart_entities.common.network.InteractPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public class PlayerInteractMultipartEntity {

    public static void attack(final Minecraft client, final InteractionHand hand, final MultipartAwareEntity entity) {
        final Vec3 pos = client.getCameraEntity().getEyePosition(client.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        final Vec3 dir = client.getCameraEntity().getViewVector(client.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        final double reach = client.player.entityInteractionRange();
        final String part = entity.getBounds().raycast(pos, pos.add(dir.scale(reach)));
        if (part != null) {
            var id = ((Entity) entity).getId();
            var interactionType = InteractionType.ATTACK;
            var isSneaking = client.player.isShiftKeyDown();
            ClientPlayNetworking.send(new InteractPacket(id, part, interactionType, hand, isSneaking));
        }
    }

    public static InteractionResult interact(final Minecraft client, final InteractionHand hand, final MultipartAwareEntity entity) {
        final Vec3 pos = client.getCameraEntity().getEyePosition(client.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        final Vec3 dir = client.getCameraEntity().getViewVector(client.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        final double reach = client.player.entityInteractionRange();
        final String part = entity.getBounds().raycast(pos, pos.add(dir.scale(reach)));
        if (part != null) {
            var id = ((Entity) entity).getId();
            var interactionType = InteractionType.INTERACT;
            var isSneaking = client.player.isShiftKeyDown();
            ClientPlayNetworking.send(new InteractPacket(id, part, interactionType, hand, isSneaking));
            return entity.interact(client.player, hand, part);
        }
        return InteractionResult.PASS;
    }

    public enum InteractionType {
        ATTACK,
        INTERACT
    }
}
