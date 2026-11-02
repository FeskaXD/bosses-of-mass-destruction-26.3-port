package net.barribob.boss.utils

import net.minecraft.world.entity.EntityTypes
import net.barribob.boss.Mod
import net.barribob.boss.block.MonolithBlock
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.item.SoulStarItem
import net.barribob.boss.item.WallTeleport
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.obsidilith.BurstAction
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithUtils
import net.barribob.boss.mob.mobs.obsidilith.PillarAction
import net.barribob.boss.mob.spawn.*
import net.barribob.boss.packets.TestPacket
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.projectile.SporeBallProjectile
import net.barribob.boss.projectile.util.ExemptEntities
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.setPos
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.HolderGetter.Provider
import net.minecraft.core.registries.Registries
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import java.util.*
import kotlin.random.Random

class InGameTests() {
    fun provideGear(source: CommandSourceStack) {
        val entity = source.playerOrException
        val armor = listOf(ItemStack(Items.NETHERITE_HELMET), ItemStack(Items.NETHERITE_CHESTPLATE), ItemStack(Items.NETHERITE_LEGGINGS), ItemStack(Items.NETHERITE_BOOTS))
        val registry = source.level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
        armor.forEach {
            it.enchant(registry.getOrThrow(Enchantments.PROTECTION), 3)
            it.enchant(registry.getOrThrow(Enchantments.UNBREAKING), 3)
        }
        val sword = ItemStack(Items.NETHERITE_SWORD)
        sword.enchant(registry.getOrThrow(Enchantments.SHARPNESS), 4)
        sword.enchant(registry.getOrThrow(Enchantments.UNBREAKING), 3)
        val bow = ItemStack(Items.BOW)
        bow.enchant(registry.getOrThrow(Enchantments.POWER), 4)
        bow.enchant(registry.getOrThrow(Enchantments.INFINITY), 1)
        bow.enchant(registry.getOrThrow(Enchantments.UNBREAKING), 3)
        val arrow = ItemStack(Items.ARROW)
        val apples = ItemStack(Items.GOLDEN_APPLE, 8)
        val food = ItemStack(Items.COOKED_PORKCHOP, 64)
        val shield = ItemStack(Items.SHIELD)
        shield.enchant(registry.getOrThrow(Enchantments.UNBREAKING), 3)
        val pickaxe = ItemStack(Items.NETHERITE_PICKAXE)
        pickaxe.enchant(registry.getOrThrow(Enchantments.EFFICIENCY), 4)
        val blocks = ItemStack(Items.COBBLESTONE, 64)
        listOf(sword, bow, apples, food, blocks, shield, pickaxe, arrow).forEach { entity.addItem(it) }
        armor.forEach { entity.addItem(it) }
    }

    fun throwProjectile(source: CommandSourceStack) {
        val entity = source.entityOrException
        if (entity is LivingEntity) {
            val projectile = SporeBallProjectile(entity, entity.level(), ExemptEntities(listOf()))
            projectile.shootFromRotation(entity, entity.xRot, entity.yRot, 0f, 1.5f, 1.0f)
            entity.level().addFreshEntity(projectile)
        }
    }

    fun spawnEntity(source: CommandSourceStack) {
        val entity = source.entityOrException
        val serverWorld = entity.level() as ServerLevel
        val compoundTag = CompoundTag()
        compoundTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getId(EntityTypes.PHANTOM).toString())

        val spawner = MobPlacementLogic(
            RangedSpawnPosition(entity.position(), 3.0, 6.0, ModRandom()),
            CompoundTagEntityProvider(compoundTag, serverWorld, Mod.LOGGER),
            MobEntitySpawnPredicate(entity.level()),
            SimpleMobSpawner(serverWorld)
        )
        spawner.tryPlacement(10)
    }

    fun burstAction(source: CommandSourceStack) {
        BurstAction(source.playerOrException).perform()
    }

    fun placePillars(source: CommandSourceStack) {
        val entity = source.playerOrException
        PillarAction(entity).perform()
    }

    fun obsidilithDeath(source: CommandSourceStack){
        val entity = source.playerOrException
        ObsidilithUtils.onDeath(entity, 100)
    }

    @Environment(EnvType.CLIENT)
    fun registerClientHandlers(){
        ClientPlayNetworking.registerGlobalReceiver(TestPacket.ID) { packet: TestPacket, context: ClientPlayNetworking.Context -> 
            testClientCallback(context.client())
        }
    }

    fun registerHandlers(){
        PayloadTypeRegistry.clientboundPlay().register(TestPacket.ID, TestPacket.codec);
    }


    fun testClient(source: CommandSourceStack) {
        PlayerLookup.around(source.level, source.position, 100.0).forEach {
            ServerPlayNetworking.send(it, TestPacket())
        }
    }

    @Environment(EnvType.CLIENT)
    private fun testClientCallback(client: Minecraft) {
        val player = client.player ?: return
        for(i in 0..10) {
            val startingRotation = Random.nextInt(360)
            ClientParticleBuilder(Particles.EYE)
                .continuousPosition { player.position().add(VecUtils.yAxis.scale(i * 0.2)).add(VecUtils.xAxis.yRot(Math.toRadians((it.getAge() * 2 + startingRotation).toDouble()).toFloat())) }
                .build(player.position().add(VecUtils.yAxis.scale(i * 0.2)).add(VecUtils.xAxis.yRot(Math.toRadians(startingRotation.toDouble()).toFloat())))
        }
    }

    fun killZombies(source: CommandSourceStack) {
        val zombie = net.minecraft.world.entity.EntityTypes.ZOMBIE.create(source.level, net.minecraft.world.entity.EntitySpawnReason.COMMAND) ?: return
        val pos = source.playerOrException.position().add(VecUtils.yAxis.scale(-5.0))
        zombie.setPos(pos)
        source.level.addFreshEntity(zombie)
        ModComponents.getWorldEventScheduler(source.level).addEvent(TimedEvent({
            zombie.hurtServer(source.level, source.level.damageSources().playerAttack(source.playerOrException), 30f)
        }, 2))
    }

    fun lichSpawn(source: CommandSourceStack){
        SoulStarItem.spawnLich(BlockPos.containing(source.position), source.level)
    }

    fun levitationPerformance(source: CommandSourceStack){
//        LevitationBlockEntity.tickFlight(source.playerOrException)
        MonolithBlock.getExplosionPower(source.level, BlockPos.containing(source.position), 2.0f)
    }

    fun wallTeleport(source: CommandSourceStack) {
        WallTeleport(source.level, source.playerOrException).tryTeleport(source.playerOrException.lookAngle, source.playerOrException.eyePosition)
    }

    fun attackRepeatedly(source: CommandSourceStack) {
        val target = source.level.getEntities(Entities.GAUNTLET) { true }.firstOrNull()
        for(i in 0..240 step 80) {
            ModComponents.getWorldEventScheduler(source.level).addEvent(TimedEvent({
               target?.hurt(source.level.damageSources().playerAttack(source.playerOrException), 9.0f)
            }, i))
        }
    }
}