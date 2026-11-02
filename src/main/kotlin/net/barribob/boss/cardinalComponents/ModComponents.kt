package net.barribob.boss.cardinalComponents

import net.barribob.boss.Mod
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer
import org.ladysnake.cca.api.v8.level.LevelComponentFactoryRegistry
import org.ladysnake.cca.api.v8.level.LevelComponentInitializer
import java.util.*

class ModComponents : LevelComponentInitializer, EntityComponentInitializer {
    companion object : IWorldEventScheduler, IPlayerMoveHistory {
        private val eventSchedulerComponentKey: ComponentKey<IWorldEventSchedulerComponent> =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                Mod.identifier("event_scheduler"),
                IWorldEventSchedulerComponent::class.java
            )

        private val playerMoveHistoryComponentKey: ComponentKey<IPlayerMoveHistoryComponent> =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                Mod.identifier("player_move_history"),
                IPlayerMoveHistoryComponent::class.java
            )

        private val chunkBlockCacheComponentKey: ComponentKey<IChunkBlockCacheComponent> =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                Mod.identifier("chunk_block_cache_component"),
                IChunkBlockCacheComponent::class.java
            )

        override fun getWorldEventScheduler(world: Level) = eventSchedulerComponentKey.get(world).get()

        override fun getPlayerPositions(serverPlayerEntity: ServerPlayer): List<Vec3> =
            playerMoveHistoryComponentKey.get(serverPlayerEntity).getHistoricalPositions()

        fun getChunkBlockCache(world: Level): Optional<IChunkBlockCacheComponent> =
            chunkBlockCacheComponentKey.maybeGet(world)
    }

    override fun registerLevelComponentFactories(registry: LevelComponentFactoryRegistry) {
        registry.register(eventSchedulerComponentKey, WorldEventScheduler::class.java, ::WorldEventScheduler)
        registry.register(chunkBlockCacheComponentKey, ChunkBlockCacheComponent::class.java, ::ChunkBlockCacheComponent)
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerFor(ServerPlayer::class.java, playerMoveHistoryComponentKey, ::PlayerMoveHistory)
    }
}