package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.barribob.boss.projectile.SporeBallProjectile
import net.minecraft.util.Mth
import com.geckolib.renderer.base.BoneSnapshots

class SporeCodeAnimations : ICodeAnimations<SporeBallProjectile> {
    override fun animate(animatable: SporeBallProjectile, partialTick: Float, bones: BoneSnapshots) {
        val pitch = if(animatable.impacted) animatable.xRot else Mth.rotLerp(partialTick, animatable.xRot - 5, animatable.xRot)
        bones.get("root1").ifPresent { it.setRotX(Math.toRadians(pitch.toDouble()).toFloat()) }
    }
}