package net.barribob.boss.mob.utils.animation

import com.geckolib.animatable.GeoAnimatable
import com.geckolib.renderer.base.BoneSnapshots

/** Code-driven bone rotations, applied each frame on top of the keyframe animations (GeckoLib 5 bone snapshots). */
fun interface ICodeAnimations<T> where T : GeoAnimatable {
    fun animate(animatable: T, partialTick: Float, bones: BoneSnapshots)
}
