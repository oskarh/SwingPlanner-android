package com.oskhoj.swingplanner.firebase.analytics

import android.app.Activity
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
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
        logEvent(eventName, mapOf(*parameters))
    }

    fun logEvent(eventName: String, parameters: Map<String, Any>? = null) {
        firebaseAnalytics.logEvent(eventName, parameters.toBundle())
    }
}