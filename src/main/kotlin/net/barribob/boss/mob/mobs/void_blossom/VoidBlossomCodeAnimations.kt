package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.minecraft.util.Mth
import com.geckolib.renderer.base.BoneSnapshots

class VoidBlossomCodeAnimations : ICodeAnimations<VoidBlossomEntity> {
    override fun animate(animatable: VoidBlossomEntity, partialTick: Float, bones: BoneSnapshots) {
        val bodyYaw = Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.visualRotationYInDegrees)
        bones.get("Leaves").ifPresent { it.setRotY(Math.toRadians(bodyYaw.toDouble()).toFloat()) }
        bones.get("Thorns").ifPresent { it.setRotY(Math.toRadians(bodyYaw.toDouble()).toFloat()) }
        bones.get("Roots").ifPresent { it.setRotY(Math.toRadians(bodyYaw.toDouble()).toFloat()) }
    }
}