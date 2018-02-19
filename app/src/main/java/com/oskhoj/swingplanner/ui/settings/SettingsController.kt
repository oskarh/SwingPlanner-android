package com.oskhoj.swingplanner.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.base.ViewType
import com.oskhoj.swingplanner.ui.base.ViewType.SETTINGS_VIEW
import com.oskhoj.swingplanner.ui.component.BottomSheetDialogHelper
import com.oskhoj.swingplanner.ui.settings.notificationmanager.SubscriptionManagerController
import com.oskhoj.swingplanner.util.ANALYTICS_ENABLE_ANIMATIONS_CLICK
import com.oskhoj.swingplanner.util.ANALYTICS_MANAGE_SUBSCRIPTIONS_CLICK
import com.oskhoj.swingplanner.util.ANALYTICS_NOTIFICATION_WINDOW_CLICK
import com.oskhoj.swingplanner.util.ANALYTICS_THEME_CLICK
import com.oskhoj.swingplanner.util.PROPERTY_IS_ENABLED
import com.oskhoj.swingplanner.util.showTapTarget
import com.oskhoj.swingplanner.util.startNotificationSettings
import kotlinx.android.synthetic.main.controller_settings.view.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.sdk21.listeners.onClick

class SettingsController(args: Bundle = Bundle.EMPTY) : ToolbarController<SettingsContract.View, SettingsContract.Presenter>(args), SettingsContract.View {
    override val presenter: SettingsContract.Presenter by instance()

    override val layoutRes = R.layout.controller_settings

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<SettingsContract.Presenter>() with instance(SettingsPresenter())
    }

    override val viewType: ViewType = SETTINGS_VIEW

    override val screenType: ScreenType = ScreenType.SETTINGS

    override fun showAppNotificationSettings() {
        AnalyticsHelper.logEvent(ANALYTICS_NOTIFICATION_WINDOW_CLICK)
        activity.startNotificationSettings()
    }

    override fun showSubscriptionsManager() {
        AnalyticsHelper.logEvent(ANALYTICS_MANAGE_SUBSCRIPTIONS_CLICK)
        router.pushController(RouterTransaction.with(SubscriptionManagerController()))
    }

    override fun showThemesDialog() {
        AnalyticsHelper.logEvent(ANALYTICS_THEME_CLICK)
        view?.let {
            longSnackbar(it, it.context.getString(R.string.themes_not_implemented))
        }
    }

    override fun showLanguageDialog(context: Context) {
        BottomSheetDialogHelper.showLanguageDialog(context)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        view.run {
            val toggleAnimationsCheckbox = animations_checkbox.apply {
                AnalyticsHelper.logEvent(ANALYTICS_ENABLE_ANIMATIONS_CLICK, PROPERTY_IS_ENABLED to isChecked)
                isChecked = AppPreferences.isAnimationsEnabled
                onClick { presenter.onAnimationsEnabledClicked(isChecked) }
            }
            toggle_animations_layout.onClick { toggleAnimationsCheckbox.performClick() }
            subscriptions_text.onClick { presenter.onSubscriptionsClicked() }
            notification_window_text.onClick { presenter.onNotificationsWindowClicked() }
            language_text.onClick {
                longSnackbar(this, context.getString(R.string.change_language_not_implemented))
//                showLanguageDialog(context)
            }
            theme_text.onClick { presenter.onThemeClicked() }
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        if (!AppPreferences.hasShownManageSubscriptionsTapTarget) {
            activity?.run {
                showTapTarget(R.id.subscriptions_text, R.string.tap_target_manage_subscriptions_title, R.string.tap_target_manage_subscriptions_message)
                AppPreferences.hasShownManageSubscriptionsTapTarget = true
            }
        }
    }
}