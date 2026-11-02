package net.barribob.boss.projectile.comet

import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.minecraft.util.Mth
import com.geckolib.renderer.base.BoneSnapshots

class CometCodeAnimations : ICodeAnimations<CometProjectile> {
    override fun animate(
        animatable: CometProjectile,
        partialTick: Float, bones: BoneSnapshots
    ) {
        val pitch = Mth.rotLerp(partialTick, animatable.xRot - 5, animatable.xRot)

        bones.get("root1").ifPresent { it.setRotX(Math.toRadians(pitch.toDouble()).toFloat()) }
    }
}