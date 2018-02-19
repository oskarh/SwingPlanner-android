package com.oskhoj.swingplanner.ui.settings

import android.content.Context
import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView

object SettingsContract {

    interface View : BaseView {
        fun showAppNotificationSettings()

        fun showSubscriptionsManager()

        fun showLanguageDialog(context: Context)

        fun showThemesDialog()
    }

    interface Presenter : Attachable<View> {
        fun onAnimationsEnabledClicked(isEnabled: Boolean)

        fun onSubscriptionsClicked()

        fun onNotificationsWindowClicked()

        fun onLanguageClicked()

        fun onThemeClicked()

        fun aboutAction()
    }
}