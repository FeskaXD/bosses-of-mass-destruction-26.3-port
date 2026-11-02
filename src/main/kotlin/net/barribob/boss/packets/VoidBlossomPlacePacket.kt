package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3

data class VoidBlossomPlacePacket(val pos: Vec3) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<VoidBlossomPlacePacket> = CustomPacketPayload.Type(Mod.identifier("void_blossom_place"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, VoidBlossomPlacePacket> = StreamCodec.of(::encode, ::decode);

        private fun encode(buf: RegistryFriendlyByteBuf, packet: VoidBlossomPlacePacket) {
            buf.writeVec3(packet.pos)
        }
        private fun decode(buf: RegistryFriendlyByteBuf): VoidBlossomPlacePacket {
            return VoidBlossomPlacePacket(buf.readVec3())
        }
    }
    
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
