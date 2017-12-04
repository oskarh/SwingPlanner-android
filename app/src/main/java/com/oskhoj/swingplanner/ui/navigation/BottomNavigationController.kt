package com.oskhoj.swingplanner.ui.navigation

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.SparseArray
import android.view.View
import android.widget.LinearLayout
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.ui.base.BaseController
import com.oskhoj.swingplanner.ui.favorites.FavoritesController
import com.oskhoj.swingplanner.ui.search.SearchController
import com.oskhoj.swingplanner.ui.settings.SettingsController
import com.oskhoj.swingplanner.ui.teachers.TeachersController
import com.oskhoj.swingplanner.util.KEY_STATE_CURRENTLY_SELECTED_ID
import com.oskhoj.swingplanner.util.KEY_STATE_NAVIGATION_BACKSTACK
import com.oskhoj.swingplanner.util.KEY_STATE_ROUTER_BUNDLES
import com.oskhoj.swingplanner.util.closeKeyboard
import com.oskhoj.swingplanner.util.firstSelectedItem
import com.oskhoj.swingplanner.util.isEmpty
import kotlinx.android.synthetic.main.controller_bottom_navigation.view.*
import timber.log.Timber
import java.util.Stack

class BottomNavigationController(args: Bundle) : BaseController<NavigationContract.View, NavigationContract.Presenter>(), NavigationContract.View {

    constructor() : this(Bundle.EMPTY)

    override val layoutRes = R.layout.controller_bottom_navigation

    override val presenter: NavigationContract.Presenter by instance()

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<NavigationContract.Presenter>() with instance(NavigationPresenter())
    }

    override fun onItemSelected(id: Int) {
        Timber.d("Navigating to $id")
        navigateTo(id)
    }

    private lateinit var bottomNavigationRoot: LinearLayout
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var controllerContainer: ChangeHandlerFrameLayout

    private var currentlySelectedItemId: Int = 0

    private var routerBundles: SparseArray<Bundle> = SparseArray(0)
    private lateinit var childRouter: Router

    private val backstack = Stack<Int>()

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        with(view) {
            bottomNavigationRoot = bottom_navigation_root
            bottomNavigationView = navigation
            controllerContainer = bottom_navigation_controller_container
        }

        bottomNavigationView.inflateMenu(R.menu.navigation)
        childRouter = getChildRouter(controllerContainer)

        if (routerBundles.isEmpty()) {
            routerBundles = SparseArray(bottomNavigationView.menu.size())
            bottomNavigationView.firstSelectedItem()?.let {
                childRouter.setRoot(RouterTransaction.with(getControllerFor(it.itemId)))
                bottomNavigationView.selectedItemId = it.itemId
                currentlySelectedItemId = bottomNavigationView.selectedItemId
            }
        } else {
            /*
             * Since we are restoring our state,
             * and onRestoreInstanceState is called before onViewBound,
             * all we need to do is rebind.
             */
            childRouter.rebindIfNeeded()
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            presenter.selectItem(item.itemId)
            true
        }
        bottomNavigationView.setOnNavigationItemReselectedListener {
            // set so we can know we only get events when selected button changes in other callback, ignore this
        }
    }

    private fun navigateTo(itemId: Int): Boolean {
        activity?.closeKeyboard()
        backstack.push(currentlySelectedItemId)
        Timber.d("Navigating ${childRouter.backstackSize} ${routerBundles.get(itemId) != null} ${routerBundles.get(itemId)?.isEmpty}")
        if (childRouter.backstackSize > 0) {
            saveChildRouter(currentlySelectedItemId)
            clearChildRouter()
        }
        currentlySelectedItemId = itemId
        val routerBundle = routerBundles.get(currentlySelectedItemId)
        if (routerBundle != null && !routerBundle.isEmpty) {
            childRouter.restoreInstanceState(routerBundle)
            childRouter.rebindIfNeeded()
        } else {
            childRouter.setRoot(RouterTransaction.with(getControllerFor(currentlySelectedItemId)))
        }
        return true
    }

    private fun saveChildRouter(itemId: Int) {
        val routerBundle = Bundle()
        childRouter.saveInstanceState(routerBundle)
        routerBundles.put(itemId, routerBundle)
    }

    private fun clearChildRouter() {
        childRouter.popToRoot()
        childRouter.popCurrentController()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.d("Saving instance state")
        saveChildRouter(currentlySelectedItemId)
        outState.putSparseParcelableArray(KEY_STATE_ROUTER_BUNDLES, routerBundles)
        outState.putInt(KEY_STATE_CURRENTLY_SELECTED_ID, currentlySelectedItemId)
        outState.putIntegerArrayList(KEY_STATE_NAVIGATION_BACKSTACK, backstack.toCollection(ArrayList()))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Timber.d("Restoring instance...")
        routerBundles = savedInstanceState.getSparseParcelableArray(KEY_STATE_ROUTER_BUNDLES)
        currentlySelectedItemId = savedInstanceState.getInt(KEY_STATE_CURRENTLY_SELECTED_ID)
        savedInstanceState.getIntegerArrayList(KEY_STATE_NAVIGATION_BACKSTACK).toCollection(backstack)
    }

    private fun getControllerFor(menuItemId: Int): Controller = when (menuItemId) {
//        R.id.navigation_browse -> instance<SearchController>().value
//        R.id.navigation_favorites -> instance<FavoritesController>().value
//        R.id.navigation_teachers -> instance<TeachersController>().value
//        R.id.navigation_settings -> instance<SettingsController>().value
        R.id.navigation_browse -> SearchController()
        R.id.navigation_favorites -> FavoritesController()
        R.id.navigation_teachers -> TeachersController()
        R.id.navigation_settings -> SettingsController()
        else -> throw IllegalStateException("Unknown bottomNavigationView item selected.")
    }

    override fun handleBack(): Boolean {
        val handleIt = !childRouter.handleBack()
        Timber.d("Backstack is $backstack, could handle it ${handleIt.not()}")
        return if (handleIt) {
            if (backstack.isNotEmpty()) {
                val itemId = backstack.pop()
                bottomNavigationView.selectedItemId = itemId
                backstack.pop()
                true
            } else {
                false
            }
        } else {
            saveChildRouter(currentlySelectedItemId)
            true
        }
    }
}
