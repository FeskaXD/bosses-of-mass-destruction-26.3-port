package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3

data class ChargedEnderPearlImpactPacket(val pos: Vec3) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<ChargedEnderPearlImpactPacket> = CustomPacketPayload.Type(Mod.identifier("charged_ender_pearl_impact"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, ChargedEnderPearlImpactPacket> = StreamCodec.of(::encode, ::decode);

        private fun encode(buf: RegistryFriendlyByteBuf, packet: ChargedEnderPearlImpactPacket) {
            buf.writeVec3(packet.pos)
        }
        private fun decode(buf: RegistryFriendlyByteBuf): ChargedEnderPearlImpactPacket {
            val pos = buf.readVec3()
            return ChargedEnderPearlImpactPacket(pos)
        }
    }
    
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
