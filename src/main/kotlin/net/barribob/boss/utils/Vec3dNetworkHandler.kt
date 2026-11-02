package net.barribob.boss.utils

import net.barribob.boss.Mod
import net.barribob.boss.packets.Vec3dPacket
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

class Vec3dNetworkHandler {

    fun clientInit() {
        ClientPlayNetworking.registerGlobalReceiver(Vec3dPacket.ID) { packet, context ->
            handleVec3dPacket(context.client(), packet)
        }
    }


    fun registerHandlers() {
        PayloadTypeRegistry.clientboundPlay().register(Vec3dPacket.ID, Vec3dPacket.CODEC);
    }

    private fun handleVec3dPacket(client: Minecraft, packet: Vec3dPacket) {
        val id = packet.id
        val pos = packet.pos

        client.execute {
            val world = client.level
            if(world != null) {
                val vecId = VecId.fromInt(id)
                if(vecId != null) vecId.effectHandler().clientHandler(world, pos)
            }
        }
    }

    fun sendVec3dPacket(world: ServerLevel, pos: Vec3, id: VecId) {
        val packet = Vec3dPacket(id.ordinal, pos)
        for (player in PlayerLookup.tracking(world, BlockPos.containing(pos))) {
            ServerPlayNetworking.send(player, packet)
        }
    }

    companion object {
        fun ServerLevel.sendVec3dPacket(pos: Vec3, id: VecId){
            Mod.vec3dNetwork.sendVec3dPacket(this, pos, id)
        }
    }
}