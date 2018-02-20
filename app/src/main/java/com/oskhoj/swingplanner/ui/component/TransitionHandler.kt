package com.oskhoj.swingplanner.ui.component

import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandler

class TransitionHandler : TransitionChangeHandler() {
    override fun getTransition(container: ViewGroup, from: View?, to: View?, isPush: Boolean): TransitionSet =
            TransitionSet()
                    .addTransition(TransitionSet()
                            .addTransition(ChangeBounds())
                            .addTransition(ChangeClipBounds())
                            .addTransition(ChangeTransform())
                            .addTransition(ChangeImageTransform())
                            .setDuration(300))
                    .setInterpolator(FastOutSlowInInterpolator())
}