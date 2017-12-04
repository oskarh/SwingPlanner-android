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
import com.oskhoj.swingplanner.ViewType
import com.oskhoj.swingplanner.ViewType.SETTINGS_VIEW
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.component.BottomSheetDialogHelper
import com.oskhoj.swingplanner.ui.settings.notificationmanager.NotificationManagerController
import kotlinx.android.synthetic.main.controller_settings.view.*
import org.jetbrains.anko.sdk21.listeners.onClick

class SettingsController(args: Bundle = Bundle.EMPTY) : ToolbarController<SettingsContract.View, SettingsContract.Presenter>(args), SettingsContract.View {
    override val presenter: SettingsContract.Presenter by instance()

    override val layoutRes = R.layout.controller_settings

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<SettingsContract.Presenter>() with instance(SettingsPresenter())
    }

    override val viewType: ViewType = SETTINGS_VIEW

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
        val enableAnimationsCheckbox = view.animations_checkbox.apply {
            isChecked = AppPreferences.isAnimationsEnabled
            onClick { presenter.onAnimationsEnabledClicked(isChecked) }
        }
        view.enable_animations_layout.apply { onClick { enableAnimationsCheckbox.performClick() } }
        view.language_text.apply { onClick { showLanguageDialog(view.context) } }
        view.subscriptions_text.apply { onClick { presenter.onSubscriptionsClicked() } }
    }
}