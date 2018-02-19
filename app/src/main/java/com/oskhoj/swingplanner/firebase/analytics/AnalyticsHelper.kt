package com.oskhoj.swingplanner.firebase.analytics

import android.app.Activity
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.oskhoj.swingplanner.firebase.notifications.NotificationType
import com.oskhoj.swingplanner.util.ANALYTICS_NOTIFICATION_EVENT_NEW
import com.oskhoj.swingplanner.util.ANALYTICS_NOTIFICATION_GENERAL
import com.oskhoj.swingplanner.util.ANALYTICS_NOTIFICATION_TEACHER_UPDATED
import com.oskhoj.swingplanner.util.toBundle

object AnalyticsHelper {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun setupFirebaseAnalytics(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun setCurrentScreen(activity: Activity, screenType: ScreenType) {
        firebaseAnalytics.setCurrentScreen(activity, screenType.screenName, screenType.screenName)
    }

    fun logEvent(eventName: String, vararg parameters: Pair<String, Any>) {
        firebaseAnalytics.logEvent(eventName, mapOf(*parameters).toBundle())
    }

    fun logEvent(notificationType: NotificationType) {
        val eventName = when (notificationType) {
            NotificationType.EVENT -> ANALYTICS_NOTIFICATION_EVENT_NEW
            NotificationType.TEACHER -> ANALYTICS_NOTIFICATION_TEACHER_UPDATED
            else -> ANALYTICS_NOTIFICATION_GENERAL
        }
        logEvent(eventName)
    }

    fun setUserProperty(userProperty: String, value: Int) {
        firebaseAnalytics.setUserProperty(userProperty, value.toString())
    }
}