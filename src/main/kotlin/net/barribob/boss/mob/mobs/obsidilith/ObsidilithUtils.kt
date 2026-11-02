package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities.OBSIDILITH
import net.barribob.boss.render.NodeBossBarRenderer
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.RandomizableContainer
import net.minecraft.sounds.SoundEvents
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import kotlin.math.pow

object ObsidilithUtils {
    private const val textureSize = 256
    private val bossBarDividerTexture = Mod.identifier("textures/gui/obsidilith_boss_bar_dividers.png")
    const val deathStatus: Byte = 3
    const val burstAttackStatus: Byte = 5
    const val waveAttackStatus: Byte = 6
    const val spikeAttackStatus: Byte = 7
    const val anvilAttackStatus: Byte = 8
    const val pillarDefenseStatus: Byte = 9
    val hpPillarShieldMilestones = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
    val obsidilithBossBarRenderer =
        NodeBossBarRenderer(OBSIDILITH.descriptionId, hpPillarShieldMilestones, bossBarDividerTexture, textureSize)
    val circlePos = MathUtils.buildBlockCircle((2.0.pow(2) + 1.0.pow(2)).pow(0.5))
    const val deathPillarHeight = 15
    const val ticksBetweenPillarLayer = 5

    fun approximatePlayerNextPosition(previousPosition: List<Vec3>, currentPos: Vec3): Vec3 {
        return previousPosition
            .map { it.subtract(currentPos).planeProject(VecUtils.yAxis) }
            .reduce { acc, vec3d -> acc.add(vec3d) }
            .scale(-0.5).add(currentPos)
    }

    fun onDeath(actor: LivingEntity, experienceDrop: Int) {
        val world = actor.level()
        if (!world.isClientSide) {
            val blockPos = actor.blockPosition()
            val vecPos = actor.position()
            val eventScheduler = ModComponents.getWorldEventScheduler(world)
            world.explode(actor, actor.x, actor.y, actor.z, 2.0f, Level.ExplosionInteraction.MOB)

            for (y in 0..deathPillarHeight) {
                eventScheduler.addEvent(TimedEvent({
                    actor.playSound(SoundEvents.STONE_PLACE, 1.0f, actor.random.randomPitch())
                    for (pos in circlePos) {
                        world.setBlock(
                            BlockPos(pos.x.toInt(), y, pos.z.toInt()).offset(blockPos),
                            Blocks.OBSIDIAN.defaultBlockState(), 3
                        )
                    }
                }, y * ticksBetweenPillarLayer))
            }

            eventScheduler.addEvent(TimedEvent({
                val chestPos = blockPos.above(deathPillarHeight + 1)
                world.setBlock(chestPos, Blocks.SHULKER_BOX.defaultBlockState(), 2)
                RandomizableContainer.setBlockEntityLootTable(world, actor.random, chestPos, Mod.loot.obsidilith)
            }, deathPillarHeight * ticksBetweenPillarLayer))

            val expTicks = 20
            val expPerTick = (experienceDrop / expTicks.toFloat()).toInt()
            val pillarTop = vecPos.add(VecUtils.yAxis.scale(deathPillarHeight.toDouble()))
            eventScheduler.addEvent(TimedEvent({
                VanillaCopies.awardExperience(
                    expPerTick,
                    pillarTop.add(RandomUtils.randVec().planeProject(VecUtils.yAxis).scale(2.0)),
                    world
                )
            }, deathPillarHeight * ticksBetweenPillarLayer, expTicks))
        }
    }
}