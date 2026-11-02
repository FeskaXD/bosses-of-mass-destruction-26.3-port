package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3

data class VoidBlossomRevivePacket(val pos: Vec3) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<VoidBlossomRevivePacket> = CustomPacketPayload.Type(Mod.identifier("void_blossom_revive"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, VoidBlossomRevivePacket> = StreamCodec.of(::encode, ::decode);

        private fun encode(buf: RegistryFriendlyByteBuf, packet: VoidBlossomRevivePacket) {
            buf.writeVec3(packet.pos)
        }
        private fun decode(buf: RegistryFriendlyByteBuf): VoidBlossomRevivePacket {
            val pos = buf.readVec3()
            return VoidBlossomRevivePacket(pos)
        }
    }
    
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
