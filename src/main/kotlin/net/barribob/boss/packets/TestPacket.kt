package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

class TestPacket : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<TestPacket> = CustomPacketPayload.Type(Mod.identifier("client_test"))
        val codec: StreamCodec<RegistryFriendlyByteBuf, TestPacket> = StreamCodec.of({ _, _ -> }, { _ -> TestPacket() })
    }
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
