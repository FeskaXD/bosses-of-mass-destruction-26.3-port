package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.util.Mth
import com.geckolib.renderer.base.BoneSnapshots

class LichCodeAnimations : ICodeAnimations<LichEntity> {
    override fun animate(animatable: LichEntity, partialTick: Float, bones: BoneSnapshots) {
        val bodyYaw = Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.visualRotationYInDegrees)
        val headYaw = Mth.rotLerp(partialTick, animatable.yHeadRotO, animatable.yHeadRot)

        val headPitch = Mth.lerp(partialTick, animatable.xRotO, animatable.xRot)

        val velocity = MathUtils.lerpVec(partialTick, animatable.velocityHistory.get(1), animatable.velocityHistory.get())

        val neutralPoseDegree = 30
        val maxDegreeVariation = 15
        val bodyPitch = (MathUtils.directionToPitch(velocity) * (maxDegreeVariation / 90.0)) + neutralPoseDegree

        val yaw = headYaw - bodyYaw
        val adjustedHeadPitch = headPitch - bodyPitch
        bones.get("code_root").ifPresent { it.setRotX(-Math.toRadians(bodyPitch).toFloat()) }
        bones.get("headBase").ifPresent { it.setRotX(-Math.toRadians(adjustedHeadPitch).toFloat()) }
        bones.get("headBase").ifPresent { it.setRotY(Math.toRadians(yaw.toDouble()).toFloat()) }
    }
}