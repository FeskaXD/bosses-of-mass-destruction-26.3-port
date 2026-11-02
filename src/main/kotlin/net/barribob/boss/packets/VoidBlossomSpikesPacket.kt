package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.core.BlockPos

data class VoidBlossomSpikesPacket(val id: Int, val pos: BlockPos) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<VoidBlossomSpikesPacket> = CustomPacketPayload.Type(Mod.identifier("void_blossom_spikes"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, VoidBlossomSpikesPacket> = StreamCodec.of(::encode, ::decode);

        private fun encode(buf: RegistryFriendlyByteBuf, packet: VoidBlossomSpikesPacket) {
            buf.writeInt(packet.id)
            buf.writeBlockPos(packet.pos)
        }
        private fun decode(buf: RegistryFriendlyByteBuf): VoidBlossomSpikesPacket {
            return VoidBlossomSpikesPacket(buf.readInt(), buf.readBlockPos())
        }
    }
    
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
