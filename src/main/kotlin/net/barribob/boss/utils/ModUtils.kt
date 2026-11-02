package net.barribob.boss.utils

import net.barribob.boss.Mod
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import net.minecraft.world.level.block.SupportType
import net.minecraft.client.multiplayer.ClientPacketListener
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.Entity
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageSources
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvent
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.levelgen.feature.Feature
import org.joml.Matrix3f
import org.joml.Matrix4f

object ModUtils {
    /**
     * Look at [ClientPacketListener.handleParticleEvent]
     */
    fun ServerLevel.spawnParticle(particleType: ParticleOptions, pos: Vec3, velOrOffset: Vec3, count: Int = 0, speed: Double = 0.0) =
        this.sendParticles(
            particleType,
            pos.x,
            pos.y,
            pos.z,
            count,
            velOrOffset.x,
            velOrOffset.y,
            velOrOffset.z,
            speed
        )
    
    fun DamageSources.shieldPiercing(world: Level, attacker: Entity) : DamageSource =
        VanillaCopies.create(world, ResourceKey.create(Registries.DAMAGE_TYPE, Mod.identifier("shield_piercing")), attacker)

    fun ServerLevel.playSound(
        pos: Vec3,
        soundEvent: SoundEvent,
        soundCategory: SoundSource,
        volume: Float,
        pitch: Float = this.random.randomPitch(),
        range: Double = if (volume > 1.0f) (16.0f * volume).toDouble() else 16.0,
        playerEntity: Player? = null,
    ) {
        val registryEntry : Holder<SoundEvent> = Holder.direct(SoundEvent.createVariableRangeEvent(soundEvent.location()))
        return this.server.playerList.broadcast(
            playerEntity,
            pos.x,
            pos.y,
            pos.z,
            range,
            dimension(),
            ClientboundSoundPacket(registryEntry, soundCategory, pos.x, pos.y, pos.z, volume, pitch, random.nextLong())
        )
    }

    fun RandomSource.randomPitch() = (this.nextFloat() - this.nextFloat()) * 0.2f + 1.0f

    fun Level.findGroundBelow(pos: BlockPos, isOpenBlock: (BlockPos) -> Boolean = { true }): BlockPos {
        for (i in pos.y downTo this.minY + 1) {
            val tempPos = BlockPos(pos.x, i, pos.z)
            if (this.getBlockState(tempPos).isFaceSturdy(this, tempPos, Direction.UP, SupportType.FULL) && isOpenBlock(tempPos.above())) {
                return tempPos
            }
        }
        return BlockPos(pos.x, this.minY, pos.z)
    }

    fun preventDespawnExceptPeaceful(entity: Mob, world: Level) {
        if (world.difficulty == Difficulty.PEACEFUL) {
            entity.discard()
        } else {
            entity.noActionTime = 0
        }
    }

    /**
     * From Maelstrom Mod ModUtils.java
     */
    fun Level.findEntitiesInLine(start: Vec3, end: Vec3, toExclude: Entity?): List<Entity> =
        this.getEntities(toExclude, AABB(start, end)) { it.boundingBox.clip(start, end).isPresent }

    val PoseStack.Pose.setNormal: Matrix3f
        get() = this.normal()

    val PoseStack.Pose.model: Matrix4f
        get() = this.pose()

    data class RotatingParticles(val pos: Vec3, val particleBuilder: ClientParticleBuilder, val minRadius: Double, val maxRadius: Double, val minSpeed: Double, val maxSpeed: Double)

    fun spawnRotatingParticles(particleParams: RotatingParticles) {
            val startingRotation = kotlin.random.Random.nextInt(360)
            val randomRadius = RandomUtils.range(particleParams.minRadius, particleParams.maxRadius)
            val rotationSpeed = RandomUtils.range(particleParams.minSpeed, particleParams.maxSpeed)
            particleParams.particleBuilder
                .continuousPosition { rotateAroundPos(particleParams.pos, it.getAge(), startingRotation, randomRadius, rotationSpeed) }
                .build(rotateAroundPos(particleParams.pos, 0, startingRotation, randomRadius, rotationSpeed), Vec3.ZERO)
    }

    private fun rotateAroundPos(
        pos: Vec3,
        age: Int,
        startingRotation: Int,
        radius: Double,
        rotationSpeed: Double
    ): Vec3 {
        val xzOffset = VecUtils.xAxis.yRot(Math.toRadians(age * rotationSpeed + startingRotation).toFloat())
        return pos.add(xzOffset.scale(radius))
    }
    
    fun addItemToGroup(item: Item) {
        CreativeModeTabEvents.modifyOutputEvent(Mod.items.itemGroup).register { it.accept(item) }
    }
 
    fun LevelReader.getConfiguredFeature(key: ResourceKey<Feature>) : Feature {
        return this.registryAccess().lookupOrThrow(Registries.FEATURE).getValueOrThrow(key)
    }
}