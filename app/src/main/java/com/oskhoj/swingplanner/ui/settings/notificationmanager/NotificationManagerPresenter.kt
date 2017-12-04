package com.oskhoj.swingplanner.ui.settings.notificationmanager

import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.ui.base.BasePresenter
import timber.log.Timber

class NotificationManagerPresenter : BasePresenter<NotificationManagerContract.View>(), NotificationManagerContract.Presenter {

    override fun addSubscription(query: String) {
        Timber.d("Adding $query")
        AppPreferences.subscriptions.add(query.trim())
    }

    override fun removeSubscription(query: String) {
        Timber.d("Removing $query")
        AppPreferences.subscriptions.remove(query.trim())
    }

    override fun aboutAction() {

    }
}