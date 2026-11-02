package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class GauntletBlindnessPacket(var id: Int, val players: List<Int>) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<GauntletBlindnessPacket> = CustomPacketPayload.Type(Mod.identifier("gauntlet_blindness"))
        
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, GauntletBlindnessPacket> = StreamCodec.of(::encode, ::decode);

        private fun encode(buf: RegistryFriendlyByteBuf, packet: GauntletBlindnessPacket) {
            buf.writeInt(packet.id)
            buf.writeVarIntArray(packet.players.toIntArray())
        }
        private fun decode(buf: RegistryFriendlyByteBuf): GauntletBlindnessPacket {
            return GauntletBlindnessPacket(buf.readInt(), buf.readVarIntArray().map { i -> i })
        }
    }
    
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
