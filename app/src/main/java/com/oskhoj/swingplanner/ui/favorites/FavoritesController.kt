package com.oskhoj.swingplanner.ui.favorites

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.ViewType
import com.oskhoj.swingplanner.ViewType.FAVORITES_VIEW
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.component.EventAdapter
import com.oskhoj.swingplanner.ui.details.DetailsController
import com.oskhoj.swingplanner.util.loadLayoutAnimation
import kotlinx.android.synthetic.main.controller_search.view.*
import timber.log.Timber

class FavoritesController(args: Bundle = Bundle.EMPTY) : ToolbarController<FavoritesContract.View, FavoritesContract.Presenter>(args), FavoritesContract.View {

    override val presenter: FavoritesContract.Presenter by instance()

    override val viewType: ViewType = FAVORITES_VIEW

    override val isToggleViewItemVisible = true

    override val layoutRes = R.layout.controller_favorites

    private val eventAdapter: EventAdapter = EventAdapter(emptyList(), {
        Timber.d("Clicked on event with id ${it.id}")
        presenter.onEventClicked(it)
    })

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<FavoritesContract.Presenter>() with provider { FavoritesPresenter(instance()) }
    }

    override fun displayEvents(events: List<EventSummary>) {
        eventAdapter.loadEvents(events)
    }

    override fun toggleViewMode(isCardView: Boolean) {
        view?.eventsRecyclerView?.run {
            eventAdapter.toggleItemViewType()
            eventAdapter.notifyDataSetChanged()
        }
    }

    override fun openEventDetails(eventSummary: EventSummary, eventDetails: EventDetails) {
        Timber.d("Opening event details for id ${eventSummary.id}")
        router.pushController(RouterTransaction.with(DetailsController(eventSummary, eventDetails)))
    }

    override fun displayEmptyView() {
        Timber.d("Displaying empty view...")
        // TODO: Show sad dancer image
    }

    override fun displayErrorView() {
        Timber.d("Show error image")
        // TODO: Show tired hamsters image
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.loadFavorites()
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        with(view.eventsRecyclerView) {
            layoutAnimation = view.loadLayoutAnimation(R.anim.layout_recycler_animation_new_dataset)
            adapter = eventAdapter
        }
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