package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.minecraft.util.Mth
import com.geckolib.renderer.base.BoneSnapshots

class GauntletCodeAnimations: ICodeAnimations<GauntletEntity> {
    override fun animate(animatable: GauntletEntity, partialTick: Float, bones: BoneSnapshots) {
        val headPitch = Mth.lerp(partialTick, animatable.xRotO, animatable.xRot)

        bones.get("codeRoot").ifPresent { it.setRotX(-Math.toRadians(headPitch.toDouble()).toFloat()) }
    }
}