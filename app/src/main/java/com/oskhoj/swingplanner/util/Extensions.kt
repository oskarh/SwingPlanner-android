package com.oskhoj.swingplanner.util

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.AnimRes
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.oskhoj.swingplanner.R
import retrofit2.Retrofit
import timber.log.Timber
import java.io.Serializable

inline fun <reified T : View> Activity.find(@IdRes resId: Int): T = findViewById(resId)

inline fun <reified T : View?> Activity.findNullable(@IdRes resId: Int): T? = findViewById<T?>(resId)

inline fun <reified T : View> View.findView(@IdRes resId: Int): T = findViewById(resId)

fun String.toBundle(value: Int) =
        Bundle().apply {
            putInt(this@toBundle, value)
        }

fun String.compareToIgnoreWhitespace(other: String, ignoreCase: Boolean = false): Int =
        trim().compareTo(other.trim(), ignoreCase)

fun <T> SparseArray<T>.isEmpty() = size() == 0

fun Activity.closeKeyboard() {
    currentFocus?.let {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun Activity.showTapTarget(@IdRes view: Int, @StringRes title: Int, @StringRes message: Int) {
    showTapTarget(findViewById<View>(view), title, message)
}

fun Activity.showTapTarget(view: View, @StringRes title: Int, @StringRes message: Int) {
    TapTargetView.showFor(this,
            TapTarget.forView(view, getString(title), getString(message))
                    .outerCircleColor(R.color.blue_grey_300)
                    .outerCircleAlpha(0.86f)
                    .targetCircleColor(R.color.white)
                    .titleTextSize(22)
                    .descriptionTextSize(18)
                    .textColor(R.color.black)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .cancelable(true)
                    .tintTarget(false)
                    .transparentTarget(false)
                    .targetRadius(60))
}

inline fun <reified T> Retrofit.create(): T = create(T::class.java)

fun ViewGroup.inflateView(@LayoutRes layoutResId: Int, attachToRoot: Boolean = false): View =
        LayoutInflater.from(context).inflate(layoutResId, this, attachToRoot)

fun Context.loadAnimation(@AnimRes animationId: Int): Animation = AnimationUtils.loadAnimation(this, animationId)

fun Context.getCompatColor(@ColorRes colorid: Int) = ContextCompat.getColor(this, colorid)

inline val Context.clipboardManager: ClipboardManager
    get() = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

inline fun <reified T : Enum<T>> enumContains(candidate: String) =
        T::class.java.enumConstants.any { it.name == candidate }

inline fun <reified T : Enum<T>> enumSetFrom(danceStyles: String) =
        danceStyles.split(",")
                .map { it.trim().toUpperCase() }
                .filter { enumContains<T>(it) }
                .map { enumValueOf<T>(it) }
                .toSet()

fun Map<String, Any>?.toBundle() =
        Bundle().apply {
            this@toBundle?.forEach {
                when (it.value) {
                    is Boolean -> putBoolean(it.key, it.value as Boolean)
                    is Byte -> putByte(it.key, it.value as Byte)
                    is Char -> putChar(it.key, it.value as Char)
                    is Double -> putDouble(it.key, it.value as Double)
                    is Float -> putFloat(it.key, it.value as Float)
                    is Int -> putInt(it.key, it.value as Int)
                    is Long -> putLong(it.key, it.value as Long)
                    is Short -> putShort(it.key, it.value as Short)
                    is String -> putString(it.key, it.value as String)
                    is Parcelable -> putParcelable(it.key, it.value as Parcelable)
                    is Serializable -> putSerializable(it.key, it.value as Serializable)
                    else -> Timber.w("Ignoring unknown parameter type for bundle with key [${it.key}]")
                }
            }
        }