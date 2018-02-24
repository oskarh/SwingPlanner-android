package com.oskhoj.swingplanner.ui.settings.notificationmanager

import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.USER_PROPERTY_NUMBER_CUSTOM_SUBSCRIPTIONS
import com.oskhoj.swingplanner.network.SubscriptionApiManager
import com.oskhoj.swingplanner.ui.base.BasePresenter
import timber.log.Timber

class SubscriptionManagerPresenter(private val subscriptionApiManager: SubscriptionApiManager) :
        BasePresenter<SubscriptionManagerContract.View>(), SubscriptionManagerContract.Presenter {

    override fun addSubscription(query: String) {
        Timber.d("Adding $query")
        AppPreferences.addSubscription(query.trim())
        subscriptionApiManager.addCustomSubscription(query.trim())
        AnalyticsHelper.setUserProperty(USER_PROPERTY_NUMBER_CUSTOM_SUBSCRIPTIONS, AppPreferences.numberCustomSubscriptions())
        view?.subscriptionAdded(query)
    }

    override fun removeSubscription(query: String) {
        Timber.d("Removing $query")
        AppPreferences.removeSubscription(query.trim())
        subscriptionApiManager.removeCustomSubscription(query.trim())
        AnalyticsHelper.setUserProperty(USER_PROPERTY_NUMBER_CUSTOM_SUBSCRIPTIONS, AppPreferences.numberCustomSubscriptions())
        view?.subscriptionRemoved(query)
    }

    override fun aboutAction() {

    }
}