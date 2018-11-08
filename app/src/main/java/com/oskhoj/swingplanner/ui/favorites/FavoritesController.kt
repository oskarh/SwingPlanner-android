package com.oskhoj.swingplanner.ui.favorites

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.base.ViewType
import com.oskhoj.swingplanner.ui.base.ViewType.FAVORITES_VIEW
import com.oskhoj.swingplanner.ui.component.EventAdapter
import com.oskhoj.swingplanner.ui.component.TransitionHandler
import com.oskhoj.swingplanner.ui.details.DetailsController
import com.oskhoj.swingplanner.util.KEY_STATE_LIST_POSITION
import com.oskhoj.swingplanner.util.NOT_SET
import com.oskhoj.swingplanner.util.animateToGone
import com.oskhoj.swingplanner.util.animateToVisible
import com.oskhoj.swingplanner.util.gone
import com.oskhoj.swingplanner.util.loadImage
import com.oskhoj.swingplanner.util.loadLayoutAnimation
import com.oskhoj.swingplanner.util.visible
import kotlinx.android.synthetic.main.controller_favorites.view.*
import timber.log.Timber

class FavoritesController(args: Bundle = Bundle.EMPTY) : ToolbarController<FavoritesContract.View, FavoritesContract.Presenter>(args), FavoritesContract.View {
    override val presenter: FavoritesContract.Presenter by instance()

    override val viewType: ViewType = FAVORITES_VIEW

    override val isToggleViewItemVisible = true

    override val layoutRes = R.layout.controller_favorites

    override val screenType: ScreenType = ScreenType.FAVORITE

    private var firstVisibleItem = NOT_SET

    private val eventAdapter: EventAdapter = EventAdapter(emptyList(), {
        Timber.d("Clicked on event with id ${it.id}")
        val transitionHandler = TransitionHandler()
        router.pushController(RouterTransaction.with(DetailsController(it))
                .pushChangeHandler(transitionHandler)
                .popChangeHandler(transitionHandler))
    })

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<FavoritesContract.Presenter>() with provider { FavoritesPresenter(instance()) }
    }

    override fun displayEvents(events: List<EventSummary>) {
        hideEmptyErrorView()
        firstVisibleItem = NOT_SET
        eventAdapter.loadEvents(events)
    }

    override fun toggleViewMode(isCardView: Boolean) {
        view?.favorites_events_recycler?.run {
            eventAdapter.toggleItemViewType()
            eventAdapter.notifyDataSetChanged()
        }
    }

    override fun displayEmptyView() {
        Timber.d("Displaying empty view...")
        showEmptyErrorView(true)
    }

    override fun displayErrorView() {
        Timber.d("Show error image")
        showEmptyErrorView(false)
    }

    private fun showEmptyErrorView(isEmptyView: Boolean) {
        firstVisibleItem = NOT_SET
        view?.run {
            favorite_empty_error_text?.run {
                text = if (isEmptyView) context.getString(R.string.no_favorites_found) else context.getString(R.string.favorite_error_text)
                visible()
            }
            favorite_empty_error_image?.visible()
            val drawable = if (isEmptyView) R.drawable.empty_image else R.drawable.error_image
            favorite_empty_error_image?.loadImage(drawable, context)
            favorites_events_recycler?.gone()
        }
    }

    private fun hideEmptyErrorView() {
        view?.run {
            favorite_empty_error_text?.gone()
            favorite_empty_error_image?.gone()
            favorites_events_recycler?.visible()
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.loadFavorites()
        if (firstVisibleItem != NOT_SET) {
            view.favorites_events_recycler.scrollToPosition(firstVisibleItem)
        }
    }

    override fun showLoading() {
        view?.favorites_progressbar?.animateToVisible()
    }

    override fun hideLoading() {
        view?.favorites_progressbar?.animateToGone()
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        with(view.favorites_events_recycler) {
            layoutAnimation = view.loadLayoutAnimation(R.anim.layout_recycler_animation_new_dataset)
            adapter = eventAdapter
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        firstVisibleItem = (view.favorites_events_recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_STATE_LIST_POSITION, firstVisibleItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        firstVisibleItem = savedInstanceState.getInt(KEY_STATE_LIST_POSITION)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.toggle_view_mode_action -> {
                    presenter.toggleListAction()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
}