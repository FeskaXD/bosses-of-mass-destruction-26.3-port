package net.barribob.boss.mob

import me.shedaniel.autoconfig.AutoConfig
import net.barribob.boss.Mod
import net.barribob.boss.animation.PauseAnimationTimer
import net.barribob.boss.config.ModConfig
import net.barribob.boss.item.ChargedEnderPearlEntity
import net.barribob.boss.item.SoulStarEntity
import net.barribob.boss.mob.mobs.gauntlet.*
import net.barribob.boss.mob.mobs.lich.*
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithArmorRenderer
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithBoneLight
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithEntity
import net.barribob.boss.mob.mobs.void_blossom.*
import net.barribob.boss.mob.utils.SimpleLivingGeoRenderer
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.ParticleFactories
import net.barribob.boss.particle.Particles
import net.barribob.boss.projectile.*
import net.barribob.boss.projectile.comet.CometCodeAnimations
import net.barribob.boss.projectile.comet.CometProjectile
import net.barribob.boss.render.*
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.data.WeakHashPredicate
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.entity.ThrownItemRenderer
import com.mojang.blaze3d.Blaze3D
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Monster
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.world.level.Level

object Entities {
    private val mobConfig = AutoConfig.getConfigHolder(ModConfig::class.java).config

    val LICH: EntityType<LichEntity> = registerConfiguredMob("lich",
        { type, world -> LichEntity(type, world, mobConfig.lichConfig) })
    { it.sized(1.8f, 3.0f).updateInterval(1) }

    val MAGIC_MISSILE: EntityType<MagicMissileProjectile> = projectile("blue_fireball", ::MagicMissileProjectile)

    val COMET: EntityType<CometProjectile> = projectile("comet", ::CometProjectile)

    val SOUL_STAR: EntityType<SoulStarEntity> = projectile("soul_star", ::SoulStarEntity)

    val CHARGED_ENDER_PEARL: EntityType<ChargedEnderPearlEntity> = projectile("charged_ender_pearl", ::ChargedEnderPearlEntity)

    val OBSIDILITH: EntityType<ObsidilithEntity> = registerConfiguredMob("obsidilith",
        { type, world -> ObsidilithEntity(type, world, mobConfig.obsidilithConfig) },
        { it.fireImmune().sized(2.0f, 4.4f) })

    val GAUNTLET: EntityType<GauntletEntity> = registerConfiguredMob("gauntlet",
        { type, world -> GauntletEntity(type, world, mobConfig.gauntletConfig) },
        { it.fireImmune().sized(5.0f, 4.0f).eyeHeight(4.0f * 0.4f) })

    val VOID_BLOSSOM: EntityType<VoidBlossomEntity> = registerConfiguredMob("void_blossom",
        { type, world -> VoidBlossomEntity(type, world, mobConfig.voidBlossomConfig) },
        { it.fireImmune().sized(8.0f, 10.0f).clientTrackingRange(3) })

    val SPORE_BALL: EntityType<SporeBallProjectile> = projectile("spore_ball", ::SporeBallProjectile)

    val PETAL_BLADE: EntityType<PetalBladeProjectile> = projectile("petal_blade", ::PetalBladeProjectile)


    private val killCounter = LichKillCounter(mobConfig.lichConfig.summonMechanic)

    private fun <T : Entity> registerConfiguredMob(
        name: String,
        factory: (EntityType<T>, Level) -> T,
        augment: (EntityType.Builder<T>) -> EntityType.Builder<T>,
    ): EntityType<T> {
        return register(name, augment(EntityType.Builder.of(EntityType.EntityFactory { t, w -> factory(t, w) }, MobCategory.MONSTER)))
    }

    private fun <T : Entity> register(name: String, builder: EntityType.Builder<T>): EntityType<T> {
        val key = ResourceKey.create(Registries.ENTITY_TYPE, Mod.identifier(name))
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key))
    }

    private fun <T : Entity> projectile(name: String, factory: EntityType.EntityFactory<T>): EntityType<T> =
        register(name, EntityType.Builder.of(factory, MobCategory.MISC).sized(0.25f, 0.25f))

    fun init() {
        if(mobConfig.lichConfig.summonMechanic.isEnabled) {
            ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(killCounter)
        }

        FabricDefaultAttributeRegistry.register(LICH,
            Monster.createMonsterAttributes()
                .add(Attributes.FLYING_SPEED, 5.0)
                .add(Attributes.MAX_HEALTH, mobConfig.lichConfig.health)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ATTACK_DAMAGE, mobConfig.lichConfig.missile.damage)
        )

        FabricDefaultAttributeRegistry.register(OBSIDILITH,
            Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, mobConfig.obsidilithConfig.health)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, mobConfig.obsidilithConfig.attack)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10.0)
                .add(Attributes.ARMOR, mobConfig.obsidilithConfig.armor)
        )

        FabricDefaultAttributeRegistry.register(GAUNTLET, Monster.createMonsterAttributes()
            .add(Attributes.FLYING_SPEED, 4.0)
            .add(Attributes.FOLLOW_RANGE, 48.0)
            .add(Attributes.MAX_HEALTH, mobConfig.gauntletConfig.health)
            .add(Attributes.KNOCKBACK_RESISTANCE, 10.0)
            .add(Attributes.ATTACK_DAMAGE, mobConfig.gauntletConfig.attack)
            .add(Attributes.ARMOR, mobConfig.gauntletConfig.armor))

        FabricDefaultAttributeRegistry.register(VOID_BLOSSOM,
            Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, mobConfig.voidBlossomConfig.health)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, mobConfig.voidBlossomConfig.attack)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10.0)
                .add(Attributes.ARMOR, mobConfig.voidBlossomConfig.armor)
        )
    }

    fun clientInit() {
        val pauseSecondTimer = PauseAnimationTimer({ Blaze3D.getTime() }, { Minecraft.getInstance().isPaused })

        EntityRendererRegistry.register(LICH) { context ->
            val texture = Mod.identifier("textures/entity/lich.png")
            SimpleLivingGeoRenderer(
                context, GeoModel(
                    { Mod.identifier("lich") },
                    { texture },
                    Mod.identifier("lich"),
                    LichCodeAnimations(),
                    { RenderTypes.entityCutout(it) }
                ),
                BoundedLighting(5),
                LichBoneLight(),
                EternalNightRenderer(),
            )
        }

        EntityRendererRegistry.register(OBSIDILITH) { context ->
            val runeColorHandler = ObsidilithBoneLight()
            val modelProvider = GeoModel<ObsidilithEntity>(
                { Mod.identifier("obsidilith") },
                { Mod.identifier("textures/entity/obsidilith.png") },
                Mod.identifier("obsidilith"),
            )
            val armorRenderer = ObsidilithArmorRenderer(modelProvider, context)
            val obsidilithRenderer = SimpleLivingGeoRenderer(
                context,
                modelProvider,
                renderer = CompositeRenderer(armorRenderer, runeColorHandler),
                renderWithModel = armorRenderer,
                iBoneLight = runeColorHandler,
                deathRotation = false
            )
            obsidilithRenderer
        }

        val missileTexture = Mod.identifier("textures/entity/blue_magic_missile.png")
        val magicMissileRenderLayer = RenderTypes.entityCutout(missileTexture)
        EntityRendererRegistry.register(MAGIC_MISSILE) { context ->
            SimpleEntityRenderer(
                context,
                CompositeRenderer(
                    BillboardRenderer(context.entityRenderDispatcher, magicMissileRenderLayer) { 0.5f },
                    ConditionalRenderer(
                        WeakHashPredicate<MagicMissileProjectile> { FrameLimiter(20f, pauseSecondTimer)::canDoFrame },
                        LerpedPosRenderer {
                            ParticleFactories.soulFlame().build(it.add(RandomUtils.randVec().scale(0.25)))
                        })
                ),
                { missileTexture },
                FullRenderLight()
            )
        }

        EntityRendererRegistry.register(SOUL_STAR) { context ->
            ThrownItemRenderer(context, 1.0f, true)
        }

        EntityRendererRegistry.register(CHARGED_ENDER_PEARL) { context ->
            ThrownItemRenderer(context, 1.0f, true)
        }

        EntityRendererRegistry.register(COMET) { context ->
            SimpleLivingGeoRenderer(context, GeoModel(
                { Mod.identifier("comet") },
                { Mod.identifier("textures/entity/comet.png") },
                Mod.identifier("comet"),
                CometCodeAnimations()
            ),
                renderer = ConditionalRenderer(
                    WeakHashPredicate { FrameLimiter(60f, pauseSecondTimer)::canDoFrame },
                    LerpedPosRenderer {
                        ParticleFactories.cometTrail().build(it.add(RandomUtils.randVec().scale(0.5)))
                    }),
                brightness = FullRenderLight()
            )
        }

        EntityRendererRegistry.register(GAUNTLET) { context ->
            val modelProvider = GeoModel(
                { Mod.identifier("gauntlet") },
                GauntletTextureProvider(),
                Mod.identifier("gauntlet"),
                GauntletCodeAnimations()
            )
            val energyRenderer = GauntletEnergyRenderer(modelProvider, context)
            val overlayOverride = GauntletOverlay()
            SimpleLivingGeoRenderer(
                context, modelProvider,
                renderer = CompositeRenderer(
                    GauntletLaserRenderer(),
                    ConditionalRenderer(
                        WeakHashPredicate { FrameLimiter(20f, pauseSecondTimer)::canDoFrame },
                        LaserParticleRenderer()
                    ),
                    energyRenderer,
                    overlayOverride
                ),
                renderWithModel = energyRenderer,
                deathRotation = false,
                overlayOverride = overlayOverride
            )
        }

        EntityRendererRegistry.register(VOID_BLOSSOM) { context ->
            val texture = Mod.identifier("textures/entity/void_blossom.png")
            val modelProvider = GeoModel(
                { Mod.identifier("void_blossom") },
                { texture },
                Mod.identifier("void_blossom"),
                VoidBlossomCodeAnimations()
            )
            val boneLight = VoidBlossomBoneLight()
            val overlay = NoRedOnDeathOverlay()
            SimpleLivingGeoRenderer(
                context, modelProvider, deathRotation = false,
                renderer = CompositeRenderer(VoidBlossomSpikeRenderer(), boneLight, overlay),
                iBoneLight = boneLight, overlayOverride = overlay
            )
        }

        EntityRendererRegistry.register(SPORE_BALL) { context ->
            val explosionFlasher = SporeBallOverlay()
            SimpleLivingGeoRenderer(context, GeoModel(
                { Mod.identifier("comet") },
                { Mod.identifier("textures/entity/spore.png") },
                Mod.identifier("comet"),
                SporeCodeAnimations()
            ),
                renderer = CompositeRenderer(ConditionalRenderer(
                    WeakHashPredicate { FrameLimiter(60f, pauseSecondTimer)::canDoFrame },
                    LerpedPosRenderer {
                        val projectileParticles = ClientParticleBuilder(Particles.OBSIDILITH_BURST)
                            .color(ModColors.GREEN)
                            .colorVariation(0.4)
                            .scale(0.5f)
                            .brightness(Particles.FULL_BRIGHT)
                        projectileParticles.build(it.add(RandomUtils.randVec().scale(0.25)), VecUtils.yAxis.scale(0.1))
                    }), explosionFlasher,
                SporeBallSizeRenderer()),
                brightness = FullRenderLight(),
                overlayOverride = explosionFlasher
            )
        }

        val petalTexture = Mod.identifier("textures/entity/petal_blade.png")
        val petalBladeRenderLayer = RenderTypes.entityCutout(petalTexture)
        EntityRendererRegistry.register(PETAL_BLADE) { context ->
            SimpleEntityRenderer(
                context,
                CompositeRenderer(
                    PetalBladeRenderer(context.entityRenderDispatcher, petalBladeRenderLayer),
                    ConditionalRenderer(
                        WeakHashPredicate { FrameLimiter(30f, pauseSecondTimer)::canDoFrame },
                        PetalBladeParticleRenderer()
                    )
                ),
                { petalTexture },
                FullRenderLight()
            )
        }
    }
}