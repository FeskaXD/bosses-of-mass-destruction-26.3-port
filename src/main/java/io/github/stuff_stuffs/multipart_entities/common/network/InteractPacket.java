package io.github.stuff_stuffs.multipart_entities.common.network;

import io.github.stuff_stuffs.multipart_entities.client.network.PlayerInteractMultipartEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;

public record InteractPacket(int id, String part, PlayerInteractMultipartEntity.InteractionType interactionType, InteractionHand hand, boolean isSneaking) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InteractPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("multipart_entities", "interact"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InteractPacket> PACKET_CODEC = StreamCodec.of(InteractPacket::write2, InteractPacket::new);

    public static void write2(RegistryFriendlyByteBuf buf, InteractPacket packet) {
        buf.writeVarInt(packet.id);
        buf.writeUtf(packet.part);
        buf.writeEnum(packet.interactionType);
        buf.writeEnum(packet.hand);
        buf.writeBoolean(packet.isSneaking);
    }
    
    public InteractPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readUtf(32767), buf.readEnum(PlayerInteractMultipartEntity.InteractionType.class), buf.readEnum(InteractionHand.class), buf.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
