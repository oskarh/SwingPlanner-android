package com.oskhoj.swingplanner.ui.component

import android.view.MotionEvent
import android.view.View
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.util.animateToSize
import com.oskhoj.swingplanner.util.getLong

class OnTouchAnimation : View.OnTouchListener {
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> view.animateToSize(0.95f, view.context.getLong(R.integer.anim_duration_medium))
            MotionEvent.ACTION_CANCEL -> view.animateToSize(1f, view.context.getLong(R.integer.anim_duration_medium))
        }
        return false
    }
}