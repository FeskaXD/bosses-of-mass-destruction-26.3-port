package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class GauntletChangeHitboxPacket(val entityId: Int, val open: Boolean) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<GauntletChangeHitboxPacket> = CustomPacketPayload.Type(Mod.identifier("change_hitbox"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, GauntletChangeHitboxPacket> = StreamCodec.of(::encode, ::decode);

        private fun encode(buf: RegistryFriendlyByteBuf, packet: GauntletChangeHitboxPacket) {
            buf.writeInt(packet.entityId);
            buf.writeBoolean(packet.open);
        }
        private fun decode(buf: RegistryFriendlyByteBuf): GauntletChangeHitboxPacket {
            return GauntletChangeHitboxPacket(buf.readInt(), buf.readBoolean());
        }
    }
    
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
