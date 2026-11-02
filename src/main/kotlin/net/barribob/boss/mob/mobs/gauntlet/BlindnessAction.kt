package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.NetworkUtils.Companion.sendBlindnessPacket
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.AABB

class BlindnessAction(
    val entity: GauntletEntity,
    val eventScheduler: EventScheduler,
    private val cancelAction: () -> Boolean,
    private val serverWorld: ServerLevel
) : IActionWithCooldown {
    override fun perform(): Int {
        serverWorld.playSound(
            entity.position(),
            Mod.sounds.gauntletCast,
            SoundSource.HOSTILE,
            3.0f,
            1.0f,
            64.0
        )

        eventScheduler.addEvent(TimedEvent(entity.hitboxHelper::setClosedFistHitbox, 10, shouldCancel = cancelAction))
        eventScheduler.addEvent(TimedEvent(entity.hitboxHelper::setOpenHandHitbox, 43))

        eventScheduler.addEvent(TimedEvent({
            val players: List<Player> = (entity.level() as net.minecraft.server.level.ServerLevel).getNearbyPlayers(
                TargetingConditions.forCombat().range(64.0),
                entity,
                AABB(entity.position(), entity.position()).inflate(64.0, 32.0, 64.0)
            )

            if (players.any()) {
                entity.sendBlindnessPacket(players)
                eventScheduler.addEvent(
                    TimedEvent({
                        players.forEach {
                            it.addEffect(
                                MobEffectInstance(
                                    MobEffects.BLINDNESS,
                                    140
                                )
                            )
                        }
                    }, 50, shouldCancel = cancelAction)
                )
            }
        }, 30, shouldCancel = cancelAction))

        return 80
    }
}