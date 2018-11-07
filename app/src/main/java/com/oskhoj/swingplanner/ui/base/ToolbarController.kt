package com.oskhoj.swingplanner.ui.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import com.github.salomonbrys.kodein.instance
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.ToolbarProvider
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_ABOUT_MENU_CLICK
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.ui.about.AboutActivity
import org.jetbrains.anko.startActivity

abstract class ToolbarController<in V : BaseView, out T : Attachable<V>>(bundle: Bundle) : BaseController<V, T>(bundle) {

    private val toolbarProvider: ToolbarProvider by instance()

    abstract val viewType: ViewType

    lateinit var menu: Menu

    open val isFilterItemVisible: Boolean = false
    open val isToggleViewItemVisible: Boolean = false

    override fun onPrepareOptionsMenu(newMenu: Menu) {
        newMenu.findItem(R.id.filter_action).isVisible = isFilterItemVisible
        val toggleItem = newMenu.findItem(R.id.toggle_view_mode_action)
        toggleItem.isVisible = isToggleViewItemVisible
        view?.run {
            val viewModeIcon = if (AppPreferences.isShowingCardList) R.drawable.ic_view_list_black_24dp else R.drawable.ic_view_module_black_24dp
            toggleItem.icon = ContextCompat.getDrawable(context, viewModeIcon)
        }
        menu = newMenu
    }

    @CallSuper
    override fun onAttach(view: View) {
        super.onAttach(view)
        toolbarProvider.updateToolbar(viewType)
    }

    @CallSuper
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.about_action -> {
                    activity?.run {
                        AnalyticsHelper.logEvent(ANALYTICS_ABOUT_MENU_CLICK)
                        startActivity<AboutActivity>()
                    }
                    true
                }
                android.R.id.home -> {
                    router.popCurrentController()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
}