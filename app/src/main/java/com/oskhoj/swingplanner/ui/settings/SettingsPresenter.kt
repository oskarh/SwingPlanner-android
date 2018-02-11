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
        view?.showSubscriptionsManager()
    }

    override fun onNotificationsWindowClicked() {
        view?.showNotificationWindow()
    }

    override fun onLanguageClicked() {

    }

    override fun onThemeClicked() {
        view?.showThemesDialog()
    }

    override fun aboutAction() {

    }
}