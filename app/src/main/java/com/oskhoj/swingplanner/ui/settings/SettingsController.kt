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
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.base.ViewType
import com.oskhoj.swingplanner.ui.base.ViewType.SETTINGS_VIEW
import com.oskhoj.swingplanner.ui.component.BottomSheetDialogHelper
import com.oskhoj.swingplanner.ui.settings.notificationmanager.NotificationManagerController
import com.oskhoj.swingplanner.util.showTapTarget
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

    override fun showSubscriptions() {

    }

    override fun showNotificationSubscriptions() {
        router.pushController(RouterTransaction.with(NotificationManagerController()))
    }

    override fun showThemesDialog() {

    }

    override fun showLanguageDialog(context: Context) {
        BottomSheetDialogHelper.showLanguageDialog(context)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        view.run {
            val toggleAnimationsCheckbox = animations_checkbox.apply {
                isChecked = AppPreferences.isAnimationsEnabled
                onClick { presenter.onAnimationsEnabledClicked(isChecked) }
            }
            toggle_animations_layout.onClick { toggleAnimationsCheckbox.performClick() }
            subscriptions_text.onClick { presenter.onSubscriptionsClicked() }
            notification_window_text.onClick { longSnackbar(this, context.getString(R.string.notification_window_not_implemented)) }
            language_text.onClick { showLanguageDialog(context) }
            theme_text.onClick { longSnackbar(this, context.getString(R.string.themes_not_implemented)) }
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