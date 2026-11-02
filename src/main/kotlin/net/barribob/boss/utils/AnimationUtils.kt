package net.barribob.boss.utils

import com.geckolib.animatable.GeoAnimatable
import com.geckolib.animation.AnimationController
import com.geckolib.animation.`object`.PlayState
import com.geckolib.animation.RawAnimation

object AnimationUtils {
    fun <T : GeoAnimatable> createIdlePredicate(animationName: String): AnimationController.AnimationStateHandler<T> = AnimationController.AnimationStateHandler {
        it.controller.setAnimation(
            RawAnimation.begin().thenLoop(animationName)
        )
        PlayState.CONTINUE
    }
}