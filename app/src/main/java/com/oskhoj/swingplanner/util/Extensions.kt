package com.oskhoj.swingplanner.util

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.support.annotation.AnimRes
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.annotation.IntegerRes
import android.support.annotation.LayoutRes
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import retrofit2.Retrofit

inline fun <reified T : View> Activity.find(@IdRes resId: Int): T = findViewById(resId)

inline fun <reified T : View?> Activity.findNullable(@IdRes resId: Int): T? = findViewById<T?>(resId)

inline fun <reified T : View> View.findView(@IdRes resId: Int): T = findViewById(resId)

fun String.toBundle(value: Int): Bundle {
    val bundle = Bundle()
    bundle.putInt(this, value)
    return bundle
}

fun <T> SparseArray<T>.isEmpty() = size() == 0

fun BottomNavigationView.firstSelectedItem(): MenuItem? =
        (0 until menu.size())
                .map { menu.getItem(it) }
                .firstOrNull { it.isChecked }

fun BottomNavigationView.setItemChecked(@IdRes itemId: Int) {
    menu.findItem(itemId)?.isChecked = true
}

fun Activity.closeKeyboard() {
    currentFocus?.let {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun TextView.addTextListener(listener: (CharSequence) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
            listener.invoke(charSequence)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
    })
}

inline fun <reified T> Retrofit.create(): T = create(T::class.java)

fun ViewGroup.inflateView(@LayoutRes layoutResId: Int, attachToRoot: Boolean = false): View =
        LayoutInflater.from(context).inflate(layoutResId, this, attachToRoot)

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

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun ImageView.loadImageOrDisappear(url: String?, context: Context) {
    url?.let {
        loadImage(it, context)
    } ?: gone()
}

fun ImageView.loadFlagIconOrDisappear(isoCode: String?, context: Context) {
    isoCode?.let {
        loadFlagIcon(it, context)
    } ?: gone()
}

fun Context.loadLayoutAnimation(@AnimRes animationId: Int): LayoutAnimationController = AnimationUtils.loadLayoutAnimation(this, animationId)

fun Context.loadAnimation(@AnimRes animationId: Int): Animation = AnimationUtils.loadAnimation(this, animationId)

fun Context.getCompatColor(@ColorRes colorid: Int) = ContextCompat.getColor(this, colorid)

fun View.getCompatColor(@ColorRes colorid: Int) = ContextCompat.getColor(context, colorid)

fun View.animateToSize(size: Float, duration: Long) {
    val animator = ObjectAnimator.ofPropertyValuesHolder(this,
            PropertyValuesHolder.ofFloat(View.SCALE_X, size),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, size))
    animator.duration = duration
    animator.start()
}

fun Context.getInteger(@IntegerRes id: Int) = resources.getInteger(id)

fun Context.getLong(@IntegerRes id: Int) = resources.getInteger(id).toLong()

inline val Context.clipboardManager: ClipboardManager
    get() = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
