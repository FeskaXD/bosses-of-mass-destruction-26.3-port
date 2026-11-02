package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3

data class PlayerVelocityPacket(val velocity: Vec3) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<PlayerVelocityPacket> = CustomPacketPayload.Type(Mod.identifier("player_velocity"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayerVelocityPacket> = StreamCodec.of(::encode, ::decode);

        private fun encode(buf: RegistryFriendlyByteBuf, packet: PlayerVelocityPacket) {
            buf.writeVec3(packet.velocity)
        }
        private fun decode(buf: RegistryFriendlyByteBuf): PlayerVelocityPacket {
            val pos = buf.readVec3()
            return PlayerVelocityPacket(pos)
        }
    }
    
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
