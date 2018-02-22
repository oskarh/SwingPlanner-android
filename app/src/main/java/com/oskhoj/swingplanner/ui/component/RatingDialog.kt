package com.oskhoj.swingplanner.ui.component

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.net.toUri
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import org.jetbrains.anko.alert

const val RATING_BAR_DIALOG_PERIOD = 5

fun showRatingDialog(context: Context) =
        context.run {
            alert(getString(R.string.rate_app_message), getString(R.string.rate_app_title)) {
                positiveButton(getString(R.string.rate_app_positive)) {
                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                        data = getString(R.string.play_store_uri).toUri()
                    })
                    AppPreferences.appStartedCount = Int.MAX_VALUE
                }
                negativeButton(getString(R.string.rate_app_negative)) {
                    AppPreferences.appStartedCount = 0
                }
                neutralPressed(getString(R.string.rate_app_never_ask)) {
                    AppPreferences.appStartedCount = Int.MAX_VALUE
                }
                onCancelled {
                    AppPreferences.appStartedCount = 0
                }
            }.show()
        }

fun shouldShowRatingDialog(activity: Activity) =
        AppPreferences.appStartedCount >= RATING_BAR_DIALOG_PERIOD && AppPreferences.appStartedCount != Int.MAX_VALUE && canHandleRatingIntent(activity)

fun canHandleRatingIntent(activity: Activity) = Intent(Intent.ACTION_VIEW).apply {
    data = activity.getString(R.string.play_store_uri).toUri()
}.resolveActivity(activity.packageManager) != null