package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3

data class VoidLilyParticlePacket(val pos: Vec3, val dir : Vec3) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<VoidLilyParticlePacket> = CustomPacketPayload.Type(Mod.identifier("void_lily_pollen"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, VoidLilyParticlePacket> = StreamCodec.of(::encode, ::decode);

        private fun encode(buf: RegistryFriendlyByteBuf, packet: VoidLilyParticlePacket) {
            buf.writeVec3(packet.pos)
            buf.writeVec3(packet.dir)
        }
        private fun decode(buf: RegistryFriendlyByteBuf): VoidLilyParticlePacket {
            return VoidLilyParticlePacket(buf.readVec3(), buf.readVec3())
        }
    }
    
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
