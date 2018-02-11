package com.oskhoj.swingplanner.util

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.net.Uri
import android.support.annotation.AnimRes
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.IntegerRes
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import android.widget.TextView
import androidx.animation.doOnEnd
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.oskhoj.swingplanner.R

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

inline fun View.visibleIf(predicate: () -> Boolean) {
    if (predicate()) {
        visible()
    } else {
        invisible()
    }
}

fun View.removeClickListener() = setOnClickListener(null)

fun View.loadLayoutAnimation(@AnimRes animationId: Int): LayoutAnimationController = AnimationUtils.loadLayoutAnimation(context, animationId)

fun View.getCompatColor(@ColorRes colorid: Int) = ContextCompat.getColor(context, colorid)

fun View.animateToSize(size: Float, animationDuration: Long, endAction: () -> Unit = {}) {
    ObjectAnimator.ofPropertyValuesHolder(this,
            PropertyValuesHolder.ofFloat(View.SCALE_X, size),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, size)).run {
        duration = animationDuration
        start()
        doOnEnd { endAction() }
    }
}

fun View.animateToVisible(animationDuration: Long = getLong(R.integer.anim_duration_very_long)) {
    visible()
    animateToSize(1f, animationDuration)
}

fun View.animateToGone(animationDuration: Long = getLong(R.integer.anim_duration_very_long)) {
    animateToSize(0f, animationDuration, { gone() })
}

fun View.getLong(@IntegerRes id: Int) = resources.getInteger(id).toLong()

fun View.restoreSize() {
    scaleX = 1f
    scaleY = 1f
}

fun View.startAnimation(@AnimRes animationResource: Int) {
    startAnimation(context.loadAnimation(animationResource))
}

fun ImageView.loadImageOrDisappear(url: String?, context: Context) {
    url?.let {
        loadImage(it, context)
    } ?: gone()
}

fun ImageView.loadImage(drawable: Int, context: Context) {
    Glide.with(context)
            .load(drawable)
            .into(this)
}

fun ImageView.loadFlagIconOrDisappear(isoCode: String?, context: Context) {
    isoCode?.let {
        loadFlagIcon(it, context)
    } ?: gone()
}

fun ImageView.loadImage(url: String, context: Context) {
    Glide.with(context)
            .load(url)
            .apply(RequestOptions.centerCropTransform())
            .into(this)
}

fun ImageView.loadFlagIcon(isoCode: String, context: Context) {
    Glide.with(context)
            .load(Uri.parse("file:///android_asset/flags/$isoCode.ico"))
            .into(this)
}

fun ImageView.setImageDrawable(@DrawableRes drawableResource: Int) =
        setImageDrawable(ContextCompat.getDrawable(context, drawableResource))

fun BottomNavigationView.firstSelectedItem(): MenuItem? =
        (0 until menu.size())
                .map { menu.getItem(it) }
                .firstOrNull { it.isChecked }

fun TextView.addTextListener(listener: (CharSequence) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
            listener(charSequence)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
    })
}