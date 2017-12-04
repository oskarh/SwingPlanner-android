package com.oskhoj.swingplanner.ui.settings

import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.ui.base.BasePresenter
import timber.log.Timber

class SettingsPresenter : BasePresenter<SettingsContract.View>(), SettingsContract.Presenter {

    override fun onAnimationsEnabledClicked(isEnabled: Boolean) {
        AppPreferences.isAnimationsEnabled = isEnabled
        Timber.d("Animations now enabled ${AppPreferences.isAnimationsEnabled}")
    }

    override fun onSubscriptionsClicked() {
        view?.showNotificationSubscriptions()
    }

    override fun onNotificationsWindowClicked() {

    }

    override fun onLanguageClicked() {

    }

    override fun onThemeClicked() {

    }

    override fun aboutAction() {

    }
}