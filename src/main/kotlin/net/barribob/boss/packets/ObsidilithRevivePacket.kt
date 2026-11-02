package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3

data class ObsidilithRevivePacket(val pos: Vec3) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<ObsidilithRevivePacket> = CustomPacketPayload.Type(Mod.identifier("obsidilith_revive"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, ObsidilithRevivePacket> = StreamCodec.of(::encode, ::decode);

        private fun encode(buf: RegistryFriendlyByteBuf, packet: ObsidilithRevivePacket) {
            buf.writeVec3(packet.pos)
        }
        private fun decode(buf: RegistryFriendlyByteBuf): ObsidilithRevivePacket {
            val pos = buf.readVec3()
            return ObsidilithRevivePacket(pos)
        }
    }
    
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
