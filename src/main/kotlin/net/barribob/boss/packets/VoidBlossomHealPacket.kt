package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3

data class VoidBlossomHealPacket(val source: Vec3, val dest : Vec3) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<VoidBlossomHealPacket> = CustomPacketPayload.Type(Mod.identifier("void_blossom_heal"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, VoidBlossomHealPacket> = StreamCodec.of(::encode, ::decode);

        private fun encode(buf: RegistryFriendlyByteBuf, packet: VoidBlossomHealPacket) {
            buf.writeVec3(packet.source)
            buf.writeVec3(packet.dest)
        }
        private fun decode(buf: RegistryFriendlyByteBuf): VoidBlossomHealPacket {
            return VoidBlossomHealPacket(buf.readVec3(), buf.readVec3())
        }
    }
    
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
