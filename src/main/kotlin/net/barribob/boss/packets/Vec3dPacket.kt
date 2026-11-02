package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3

data class Vec3dPacket(var id: Int, val pos: Vec3) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<Vec3dPacket> = CustomPacketPayload.Type(Mod.identifier("client_vec3d"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, Vec3dPacket> = StreamCodec.of(::encode, ::decode);

        private fun encode(buf: RegistryFriendlyByteBuf, packet: Vec3dPacket) {
            buf.writeInt(packet.id)
            buf.writeVec3(packet.pos)
        }
        private fun decode(buf: RegistryFriendlyByteBuf): Vec3dPacket {
            return Vec3dPacket(buf.readInt(), buf.readVec3())
        }
    }
    
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
