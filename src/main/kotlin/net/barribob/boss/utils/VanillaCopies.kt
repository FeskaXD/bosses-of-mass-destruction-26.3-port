package net.barribob.boss.utils

import net.barribob.boss.utils.ModUtils.model
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.world.level.block.Block; import net.minecraft.world.level.block.Blocks; import net.minecraft.world.level.block.DoublePlantBlock; import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.client.particle.SingleQuadParticle
import com.mojang.blaze3d.vertex.VertexConsumer; import net.minecraft.client.Camera; import net.barribob.boss.render.legacy.MultiBufferSource; import net.minecraft.client.renderer.rendertype.RenderType; import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.entity.DragonFireballRenderer
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.GuardianRenderer
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType

import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.tags.BlockTags
import net.minecraft.world.phys.HitResult
import com.mojang.math.Axis; import net.minecraft.core.BlockPos; import net.minecraft.util.Mth; import net.minecraft.world.phys.AABB; import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.BlockGetter; import net.minecraft.world.level.ClipContext; import net.minecraft.world.level.gamerules.GameRules; import net.minecraft.world.level.Level; import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.ClipContext.Fluid
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

object VanillaCopies {

    fun create(world: Level, key: ResourceKey<DamageType>, attacker: Entity?): DamageSource {
        return DamageSource(world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key), attacker)
    }

    /**
     * FlyingMob.travel (1.21)
     */
    fun travel(movementInput: Vec3, entity: LivingEntity, baseFrictionCoefficient: Float = 0.91f) {
        when {
            entity.isInWater -> {
                entity.moveRelative(0.02f, movementInput)
                entity.move(MoverType.SELF, entity.deltaMovement)
                entity.deltaMovement = entity.deltaMovement.scale(0.800000011920929)
            }
            entity.isInLava -> {
                entity.moveRelative(0.02f, movementInput)
                entity.move(MoverType.SELF, entity.deltaMovement)
                entity.deltaMovement = entity.deltaMovement.scale(0.5)
            }
            else -> {
                val friction = if (entity.onGround()) {
                    entity.level().getBlockState(BlockPos.containing(entity.x, entity.y - 1.0, entity.z)).block
                        .friction * baseFrictionCoefficient
                } else {
                    baseFrictionCoefficient
                }
                val g = 0.16277137f / (friction * friction * friction)

                entity.moveRelative(if (entity.onGround()) 0.1f * g else 0.02f, movementInput)
                entity.move(MoverType.SELF, entity.deltaMovement)
                entity.deltaMovement = entity.deltaMovement.scale(friction.toDouble())
            }
        }
        entity.calculateEntityAnimation(false)
    }

    /**
     * Adapted from [Mob.lookAt]
     */
    fun Mob.lookAtTarget(target: Vec3, maxYawChange: Float, maxPitchChange: Float) {
        val d: Double = target.x - this.x
        val e: Double = target.z - this.z
        val g: Double = target.y - this.eyeY

        val h = sqrt(d * d + e * e)
        val i = (Mth.atan2(e, d) * 57.2957763671875).toFloat() - 90.0f
        val j = (-(Mth.atan2(g, h) * 57.2957763671875)).toFloat()
        this.xRot = changeAngle(this.xRot, j, maxPitchChange)
        this.yRot = changeAngle(this.yRot, i, maxYawChange)
    }

    /**
     * [Mob.changeAngle]
     */
    private fun changeAngle(oldAngle: Float, newAngle: Float, maxChangeInAngle: Float): Float {
        var f = Mth.wrapDegrees(newAngle - oldAngle)
        if (f > maxChangeInAngle) {
            f = maxChangeInAngle
        }
        if (f < -maxChangeInAngle) {
            f = -maxChangeInAngle
        }
        return oldAngle + f
    }

    /**
     * [ClientPacketListener.handleAddEntity]
     */
    fun handleClientSpawnEntity(client: Minecraft, packet: ClientboundAddEntityPacket) {
        val d: Double = packet.x
        val e: Double = packet.y
        val f: Double = packet.z
        val entityType = packet.type
        val world = client.level ?: return

        val entity15 = entityType.create(world, net.minecraft.world.entity.EntitySpawnReason.LOAD)

        if (entity15 != null) {
            val i: Int = packet.id
            entity15.syncPacketPositionCodec(d, e, f)
            entity15.snapTo(d, e, f)
            entity15.xRot = (packet.xRot * 360) / 256.0f
            entity15.yRot = (packet.yRot * 360) / 256.0f
            entity15.id = i
            entity15.uuid = packet.uuid
            world.addEntity(entity15)
        }
    }

    /**
     * [DragonFireballRenderer.render]
     */
    fun renderBillboard(
        matrixStack: PoseStack,
        vertexConsumerProvider: MultiBufferSource,
        i: Int,
        dispatcher: EntityRenderDispatcher,
        layer: RenderType,
        rotation: Quaternionf = Quaternionf()
    ) {
        matrixStack.pushPose()
        matrixStack.rotate(dispatcher.camera!!.rotation())
        matrixStack.rotate(Axis.YP.rotationDegrees(180.0f))
        matrixStack.rotate(rotation)
        val entry = matrixStack.last()
        val matrix4f = entry.model
        val matrix3f = entry.normal()
        val vertexConsumer = vertexConsumerProvider.getBuffer(layer)
        produceVertex(vertexConsumer, matrix4f, entry, i, 0.0f, 0, 0, 1)
        produceVertex(vertexConsumer, matrix4f, entry, i, 1.0f, 0, 1, 1)
        produceVertex(vertexConsumer, matrix4f, entry, i, 1.0f, 1, 1, 0)
        produceVertex(vertexConsumer, matrix4f, entry, i, 0.0f, 1, 0, 0)
        matrixStack.popPose()
    }

    /**
     * [DragonFireballRenderer.produceVertex]
     */
    private fun produceVertex(
        vertexConsumer: VertexConsumer,
        modelMatrix: Matrix4f,
        normalMatrix: PoseStack.Pose,
        light: Int,
        x: Float,
        y: Int,
        textureU: Int,
        textureV: Int,
    ) {
        vertexConsumer.addVertex(modelMatrix, x - 0.5f, y.toFloat() - 0.25f, 0.0f).setColor(255, 255, 255, 255)
            .setUv(
                textureU.toFloat(),
                textureV.toFloat()
            ).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
            .setNormal(normalMatrix, 0.0f, 1.0f, 0.0f)
    }

    /**
     * [LivingEntity.hasLineOfSight]
     */
    fun hasDirectLineOfSight(to: Vec3, from: Vec3, world: BlockGetter, entity: Entity): Boolean {
        return world.clip(
            ClipContext(
                to,
                from,
                ClipContext.Block.COLLIDER,
                Fluid.NONE,
                entity
            )
        ).type == HitResult.Type.MISS
    }

    /**
     * [EntityRenderer.getBlockLight]
     */
    fun getBlockLight(entity: Entity, blockPos: BlockPos?): Int {
        return if (entity.isOnFire) 15 else entity.level().getBrightness(LightLayer.BLOCK, blockPos!!)
    }

    /**
     * Adapted from [EnderDragon.awardExperience]
     *
     * No longer is present perhaps changed to [EnderDragon.tickDeath]?
     */
    fun awardExperience(amount: Int, pos: Vec3, world: Level) {
        var amt = amount
        while (amt > 0) {
            val i = ExperienceOrb.getExperienceValue(amt)
            amt -= i
            world.addFreshEntity(ExperienceOrb(world, pos.x, pos.y, pos.z, i))
        }
    }

    /**
     * [SingleQuadParticle.render] without rotation
     */
    fun buildFlatGeometry(
        camera: Camera, tickDelta: Float,
        prevPosX: Double,
        prevPosY: Double,
        prevPosZ: Double,
        x: Double,
        y: Double,
        z: Double,
        scale: Float,
        rotation: Float
    ): Array<Vector3f> {
        val vec3d = camera.position()
        val f = (Mth.lerp(tickDelta.toDouble(), prevPosX, x) - vec3d.x()).toFloat()
        val g = (Mth.lerp(tickDelta.toDouble(), prevPosY, y) - vec3d.y()).toFloat()
        val h = (Mth.lerp(tickDelta.toDouble(), prevPosZ, z) - vec3d.z()).toFloat()

        val vector3fs = arrayOf(
            Vector3f(-1.0f, 0.0f, -1.0f),
            Vector3f(-1.0f, 0.0f, 1.0f),
            Vector3f(1.0f, 0.0f, 1.0f),
            Vector3f(1.0f, 0.0f, -1.0f)
        )

        for (k in 0..3) {
            val vector3f2 = vector3fs[k]
            vector3f2.rotate(Axis.YP.rotationDegrees(rotation))
            vector3f2.mul(scale)
            vector3f2.add(f, g, h)
        }

        return vector3fs
    }

    /**
     * [SingleQuadParticle.render]
     */
    fun buildBillboardGeometry(
        camera: Camera, tickDelta: Float,
        prevPosX: Double,
        prevPosY: Double,
        prevPosZ: Double,
        x: Double,
        y: Double,
        z: Double,
        scale: Float,
        rotation: Float
    ): Array<Vector3f> {
        val vec3d = camera.position()
        val f = (Mth.lerp(tickDelta.toDouble(), prevPosX, x) - vec3d.x()).toFloat()
        val g = (Mth.lerp(tickDelta.toDouble(), prevPosY, y) - vec3d.y()).toFloat()
        val h = (Mth.lerp(tickDelta.toDouble(), prevPosZ, z) - vec3d.z()).toFloat()
        val quaternion2: Quaternionf = camera.rotation()

        val vector3fs = arrayOf(
            Vector3f(1.0f, -1.0f, 0.0f),
            Vector3f(1.0f, 1.0f, 0.0f),
            Vector3f(-1.0f, 1.0f, 0.0f),
            Vector3f(-1.0f, -1.0f, 0.0f),
        )

        for (k in 0..3) {
            val vector3f2 = vector3fs[k]
            vector3f2.rotate(Axis.ZP.rotationDegrees(rotation))
            vector3f2.rotate(quaternion2)
            vector3f2.mul(scale)
            vector3f2.add(f, g, h)
        }

        return vector3fs
    }

    /**
     * [EnderDragon.destroyBlocks]
     */
    fun Entity.destroyBlocks(box: AABB): Boolean {
        val i = Mth.floor(box.minX)
        val j = Mth.floor(box.minY)
        val k = Mth.floor(box.minZ)
        val l = Mth.floor(box.maxX)
        val m = Mth.floor(box.maxY)
        val n = Mth.floor(box.maxZ)
        var bl = false
        var bl2 = false
        for (o in i..l) {
            for (p in j..m) {
                for (q in k..n) {
                    val blockPos = BlockPos(o, p, q)
                    val blockState: BlockState = this.level().getBlockState(blockPos)
                    if (!blockState.isAir && blockState.block != Blocks.FIRE) {
                        if ((this.level() as net.minecraft.server.level.ServerLevel).gameRules.get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING)
                            && !blockState.`is`(BlockTags.WITHER_IMMUNE)
                        ) {
                            bl2 = this.level().destroyBlock(blockPos, false) || bl2
                        } else {
                            bl = true
                        }
                    }
                }
            }
        }
        return bl
    }

    /**
     * [GuardianRenderer.render]
     */
    fun renderBeam(actor: LivingEntity, target: Vec3, prevTarget: Vec3, tickDelta: Float, color: Vec3, matrixStack: PoseStack, vertexConsumerProvider: MultiBufferSource, layer: RenderType) {
            val j: Float = actor.level().gameTime.toFloat() + tickDelta
            val k = j % 1.0f
            val l: Float = actor.eyeHeight
            matrixStack.pushPose()
            matrixStack.translate(0.0, l.toDouble(), 0.0)
            val vec3d: Vec3 = MathUtils.lerpVec(tickDelta, prevTarget, target)
            val vec3d2: Vec3 = this.fromLerpedPosition(actor, l.toDouble(), tickDelta)
            var vec3d3 = vec3d.subtract(vec3d2)
            val m = vec3d3.length().toFloat()
            vec3d3 = vec3d3.normalize()
            val n = acos(vec3d3.y).toFloat()
            val o = atan2(vec3d3.z, vec3d3.x).toFloat()
            matrixStack.rotate(Axis.YP.rotationDegrees((1.5707964f - o) * 57.295776f))
            matrixStack.rotate(Axis.XP.rotationDegrees(n * 57.295776f))
            val q = j * 0.05f * -1.5f
            val red = (color.x * 255).toInt()
            val green = (color.y * 255).toInt()
            val blue = (color.z * 255).toInt()
            val x = Mth.cos((q + 2.3561945f).toDouble()) * 0.282f
            val y = Mth.sin((q + 2.3561945f).toDouble()) * 0.282f
            val z = Mth.cos((q + 0.7853982f).toDouble()) * 0.282f
            val aa = Mth.sin((q + 0.7853982f).toDouble()) * 0.282f
            val ab = Mth.cos((q + 3.926991f).toDouble()) * 0.282f
            val ac = Mth.sin((q + 3.926991f).toDouble()) * 0.282f
            val ad = Mth.cos((q + 5.4977875f).toDouble()) * 0.282f
            val ae = Mth.sin((q + 5.4977875f).toDouble()) * 0.282f
            val af = Mth.cos((q + 3.1415927f).toDouble()) * 0.2f
            val ag = Mth.sin((q + 3.1415927f).toDouble()) * 0.2f
            val ah = Mth.cos((q + 0.0f).toDouble()) * 0.2f
            val ai = Mth.sin((q + 0.0f).toDouble()) * 0.2f
            val aj = Mth.cos((q + 1.5707964f).toDouble()) * 0.2f
            val ak = Mth.sin((q + 1.5707964f).toDouble()) * 0.2f
            val al = Mth.cos((q + 4.712389f).toDouble()) * 0.2f
            val am = Mth.sin((q + 4.712389f).toDouble()) * 0.2f
            val aq = -1.0f - k // Negated K to reverse direction of laser movement
            val ar = m * 2.5f + aq
            val vertexConsumer: VertexConsumer = vertexConsumerProvider.getBuffer(layer)
            val entry: PoseStack.Pose = matrixStack.last()
            val matrix4f = entry.model
            val matrix3f = entry.normal()
            method_23173(vertexConsumer, matrix4f, entry, af, m, ag, red, green, blue, 0.4999f, ar)
            method_23173(vertexConsumer, matrix4f, entry, af, 0.0f, ag, red, green, blue, 0.4999f, aq)
            method_23173(vertexConsumer, matrix4f, entry, ah, 0.0f, ai, red, green, blue, 0.0f, aq)
            method_23173(vertexConsumer, matrix4f, entry, ah, m, ai, red, green, blue, 0.0f, ar)
            method_23173(vertexConsumer, matrix4f, entry, aj, m, ak, red, green, blue, 0.4999f, ar)
            method_23173(vertexConsumer, matrix4f, entry, aj, 0.0f, ak, red, green, blue, 0.4999f, aq)
            method_23173(vertexConsumer, matrix4f, entry, al, 0.0f, am, red, green, blue, 0.0f, aq)
            method_23173(vertexConsumer, matrix4f, entry, al, m, am, red, green, blue, 0.0f, ar)
            var `as` = 0.0f
            if (actor.tickCount % 2 == 0) {
                `as` = 0.5f
            }
            method_23173(vertexConsumer, matrix4f, entry, x, m, y, red, green, blue, 0.5f, `as` + 0.5f)
            method_23173(
                vertexConsumer,
                matrix4f,
                entry,
                z,
                m,
                aa,
                red,
                green,
                blue,
                1.0f,
                `as` + 0.5f
            )
            method_23173(vertexConsumer, matrix4f, entry, ad, m, ae, red, green, blue, 1.0f, `as`)
            method_23173(vertexConsumer, matrix4f, entry, ab, m, ac, red, green, blue, 0.5f, `as`)
            matrixStack.popPose()
    }

    /**
     * [GuardianRenderer.method_23173]
     */
    fun method_23173(
        vertexConsumer: VertexConsumer,
        matrix4f: Matrix4f,
        matrix3f: PoseStack.Pose,
        f: Float,
        g: Float,
        h: Float,
        i: Int,
        j: Int,
        k: Int,
        l: Float,
        m: Float
    ) {
        vertexConsumer.addVertex(matrix4f, f, g, h)
            .setColor(i, j, k, 255)
            .setUv(l, m)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(15728800)
            .setNormal(matrix3f, 0.0f, 0.0f, -1.0f) // Changed from setNormal(0, 1, 0) because that brightened it for some reason that I cannot fathom with my pathetic opengl knowledge
            
    }

    /**
     * [GuardianRenderer.fromLerpedPosition]
     */
    fun fromLerpedPosition(entity: LivingEntity, yOffset: Double, delta: Float): Vec3 {
        val d = Mth.lerp(delta.toDouble(), entity.xOld, entity.x)
        val e = Mth.lerp(delta.toDouble(), entity.yOld, entity.y) + yOffset
        val f = Mth.lerp(delta.toDouble(), entity.zOld, entity.z)
        return Vec3(d, e, f)
    }

    /**
     * [DoublePlantBlock.onBreakInCreative]
     */
    fun onBreakInCreative(world: Level, pos: BlockPos, state: BlockState, player: Player?) {
        val doubleBlockHalf = state.getValue(DoublePlantBlock.HALF) as DoubleBlockHalf
        if (doubleBlockHalf == DoubleBlockHalf.UPPER) {
            val blockPos = pos.below()
            val blockState = world.getBlockState(blockPos)
            if (blockState.block === state.block && blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 35)
                world.levelEvent(player, 2001, blockPos, Block.getId(blockState))
            }
        }
    }
}