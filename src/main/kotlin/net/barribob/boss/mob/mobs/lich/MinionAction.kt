package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.spawn.*
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.minecraft.world.entity.Entity
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.TagParser
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.Vec3

class MinionAction(
    private val entity: LichEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) : IActionWithCooldown {

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayer) return minionSummonCooldown
        performMinionSummon(target)
        return minionSummonCooldown
    }

    private fun performMinionSummon(target: ServerPlayer) {
        eventScheduler.addEvent(
            TimedEvent({ beginSummonSingleMob(target) },
                minionSummonDelay,
                shouldCancel = shouldCancel))
    }

    fun beginSummonSingleMob(target: ServerPlayer) {
        val compoundTag = summonNbt.copy()
        compoundTag.putString("id", summonId)
        val mobSpawner = SimpleMobSpawner(target.level())
        val entityProvider = CompoundTagEntityProvider(compoundTag, target.level(), Mod.LOGGER)
        val spawnPredicate = MobEntitySpawnPredicate(target.level())
        val summonCircleBeforeSpawn: (pos: Vec3, entity: Entity) -> Unit = { pos, summon ->
            target.level().spawnParticle(Particles.LICH_MAGIC_CIRCLE, pos, Vec3.ZERO)
            target.level().playSound(pos, Mod.sounds.minionRune, SoundSource.HOSTILE, 1.0f, range = 64.0)
            eventScheduler.addEvent(
                TimedEvent({
                    mobSpawner.spawn(pos, summon)
                    entity.playSound(Mod.sounds.minionSummon, 0.7f, 1.0f)
                }, minionRuneToMinionSpawnDelay, shouldCancel = shouldCancel))
        }

        MobPlacementLogic(
            RangedSpawnPosition(target.position(), 4.0, 8.0, ModRandom()),
            entityProvider,
            spawnPredicate,
            summonCircleBeforeSpawn
        ).tryPlacement(30)
    }

    companion object {
        const val minionRuneToMinionSpawnDelay = 40
        const val minionSummonDelay = 40
        const val minionSummonParticleDelay = 10
        const val minionSummonCooldown = 80
        const val summonId = "minecraft:phantom"
        val summonNbt: CompoundTag = TagParser.parseCompoundFully("{Health:14,Size:2,Attributes:[{Name:\"generic.max_health\",Base:14f}]}")
        val summonEntityType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(summonId))
    }
}