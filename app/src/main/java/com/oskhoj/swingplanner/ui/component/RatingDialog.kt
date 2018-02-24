package com.oskhoj.swingplanner.ui.component

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.net.toUri
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_RATE_ACCEPTED
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_RATE_CANCELLED
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_RATE_DECLINED
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_RATE_NEVER_ASK
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_RATE_OFFERED
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.util.REMOTE_CONFIG_RATING_PERIOD
import org.jetbrains.anko.alert

val RATING_BAR_DIALOG_PERIOD: Long = FirebaseRemoteConfig.getInstance().getLong(REMOTE_CONFIG_RATING_PERIOD)

fun showRatingDialog(context: Context) =
        context.run {
            AnalyticsHelper.logEvent(ANALYTICS_RATE_OFFERED)
            alert(getString(R.string.rate_app_message), getString(R.string.rate_app_title)) {
                positiveButton(getString(R.string.rate_app_positive)) {
                    AnalyticsHelper.logEvent(ANALYTICS_RATE_ACCEPTED)
                    redirectToPlayStore()
                }
                negativeButton(getString(R.string.rate_app_negative)) {
                    AnalyticsHelper.logEvent(ANALYTICS_RATE_DECLINED)
                    AppPreferences.appStartedCount = 0
                }
                neutralPressed(getString(R.string.rate_app_never_ask)) {
                    AnalyticsHelper.logEvent(ANALYTICS_RATE_NEVER_ASK)
                    AppPreferences.appStartedCount = Int.MAX_VALUE
                }
                onCancelled {
                    AnalyticsHelper.logEvent(ANALYTICS_RATE_CANCELLED)
                    AppPreferences.appStartedCount = 0
                }
            }.show()
        }

fun Context.redirectToPlayStore() {
    AppPreferences.appStartedCount = Int.MAX_VALUE
    startActivity(Intent(Intent.ACTION_VIEW).apply {
        data = getString(R.string.play_store_uri).toUri()
    })
}

fun shouldShowRatingDialog(activity: Activity) =
        AppPreferences.appStartedCount >= RATING_BAR_DIALOG_PERIOD && AppPreferences.appStartedCount != Int.MAX_VALUE && canHandleRatingIntent(activity)

fun canHandleRatingIntent(activity: Activity) = Intent(Intent.ACTION_VIEW).apply {
    data = activity.getString(R.string.play_store_uri).toUri()
}.resolveActivity(activity.packageManager) != null