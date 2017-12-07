package com.oskhoj.swingplanner.ui.search

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.ViewType
import com.oskhoj.swingplanner.ViewType.SEARCH_VIEW
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.SearchEventsPage
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.component.BottomSheetDialogHelper
import com.oskhoj.swingplanner.ui.component.HeaderEventAdapter
import com.oskhoj.swingplanner.ui.component.TextChangedListener
import com.oskhoj.swingplanner.ui.details.DetailsController
import com.oskhoj.swingplanner.util.KEY_STATE_EVENTS_LIST
import com.oskhoj.swingplanner.util.KEY_STATE_LIST_POSITION
import com.oskhoj.swingplanner.util.KEY_STATE_SEARCH_TEXT
import com.oskhoj.swingplanner.util.closeKeyboard
import com.oskhoj.swingplanner.util.invisible
import com.oskhoj.swingplanner.util.loadLayoutAnimation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controller_search.*
import kotlinx.android.synthetic.main.controller_search.view.*
import timber.log.Timber

class SearchController(args: Bundle = Bundle.EMPTY) : ToolbarController<SearchContract.View, SearchContract.Presenter>(args), SearchContract.View {
    override val presenter: SearchContract.Presenter by instance()

    override val isFilterItemVisible = true
    override val isToggleViewItemVisible = true

    override val layoutRes = R.layout.controller_search

    override val viewType: ViewType = SEARCH_VIEW

    private var recyclerView: RecyclerView? = null
    private lateinit var backIcon: AppCompatImageView
    private var searchText: EditText? = null

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<SearchContract.Presenter>() with provider { SearchPresenter(instance()) }
    }

    private lateinit var clearIcon: AppCompatImageView
    private val textListener = TextChangedListener {
        clearIcon.visibility = if (it.isEmpty()) View.INVISIBLE else View.VISIBLE
        presenter.searchEvents(it)
    }

    private val eventAdapter: HeaderEventAdapter = HeaderEventAdapter(emptyList(), {
        Timber.d("Clicked on event with id ${it.id}")
        presenter.onEventClicked(it)
    })

    private var listState: Parcelable? = null

    private var storedText: String = ""

    override fun displayEvents(searchPage: SearchEventsPage) {
        eventAdapter.loadEventsPage(searchPage.eventsPage)
    }

    override fun toggleViewMode(isCardView: Boolean) {
        recyclerView?.run {
            updateMenuItemIcon(isCardView)
            eventAdapter.toggleItemViewType()
            eventAdapter.notifyDataSetChanged()
        }
    }

    override fun abortSearch() {
        clearText()
        searchText?.clearFocus()
        activity?.closeKeyboard()
        backIcon.invisible()
    }

    override fun openEventDetails(eventSummary: EventSummary, eventDetails: EventDetails) {
        Timber.d("Opening event details for id ${eventSummary.id}")
        activity?.closeKeyboard()
        router.pushController(RouterTransaction.with(DetailsController(eventSummary, eventDetails)))
    }

    override fun displayEmptyView() {
        Timber.d("Displaying empty view...")
    }

    override fun displayErrorView() {
        Timber.d("Displaying error view...")
    }

    override fun clearText() {
        searchText?.text?.clear()
        clearIcon.invisible()
    }

    override fun showFilterDialog() {
        BottomSheetDialogHelper.showFilterDialog(view?.context)
    }

    private fun updateMenuItemIcon(isCardView: Boolean) {
        activity?.let {
            val toggleIcon = if (isCardView) R.drawable.ic_view_list_black_24dp else R.drawable.ic_view_module_black_24dp
            with(menu.findItem(R.id.toggle_view_mode_action)) {
                icon = ContextCompat.getDrawable(it, toggleIcon)
            }
        }
    }

    private fun setUpRecyclerView(view: View) {
        with(view.eventsRecyclerView) {
            layoutAnimation = view.loadLayoutAnimation(R.anim.layout_recycler_animation_new_dataset)
            adapter = eventAdapter
            listState?.let {
                Timber.d("Restoring list state...")
                layoutManager.onRestoreInstanceState(it)
            }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy != 0) {
                        activity?.closeKeyboard()
                    }
                    if (!recyclerView.canScrollVertically(RecyclerView.VERTICAL)) {
                        Timber.d("End of list...")
                    }
                }
            })
        }
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        Timber.d("onViewBound")
        setUpRecyclerView(view)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        Timber.d("Attaching view..")
        activity?.run {
            recyclerView = eventsRecyclerView
            backIcon = search_back
            backIcon.setOnClickListener { presenter.onSearchBack() }
            clearIcon = search_clear
            clearIcon.setOnClickListener { presenter.onSearchClear() }

            search_text?.run {
                searchText = this
                setText(storedText)
                setSelection(storedText.length)
                addTextChangedListener(textListener)
                setOnFocusChangeListener { _, hasFocus ->
                    backIcon.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
                    clearIcon.visibility = if (hasFocus && text.isNotBlank()) View.VISIBLE else View.INVISIBLE
                }
                if (eventAdapter.isEmpty()) {
                    presenter.searchEvents(storedText)
                }
            }
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        backIcon.setOnClickListener(null)
        clearIcon.setOnClickListener(null)
        searchText?.removeTextChangedListener(textListener)
        searchText?.onFocusChangeListener = null
    }

    // TODO: Check if searchText has been initialized
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(KEY_STATE_EVENTS_LIST, ArrayList<EventSummary>(eventAdapter.events))
        val searchString = searchText?.text?.toString() ?: ""
        outState.putString(KEY_STATE_SEARCH_TEXT, searchString)
        val listState = recyclerView?.layoutManager?.onSaveInstanceState()
        outState.putParcelable(KEY_STATE_LIST_POSITION, listState)
        Timber.d("onSaveInstanceState, saving ${eventAdapter.events.size} events")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Timber.d("Size was ${savedInstanceState.getParcelableArrayList<EventSummary>(KEY_STATE_EVENTS_LIST).size}")
        storedText = savedInstanceState.getString(KEY_STATE_SEARCH_TEXT)
        val storedEvents = savedInstanceState.getParcelableArrayList<EventSummary>(KEY_STATE_EVENTS_LIST) as ArrayList<EventSummary>
        eventAdapter.loadEvents(storedEvents)
        listState = savedInstanceState.getParcelable(KEY_STATE_LIST_POSITION)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.filter_action -> {
                    presenter.filterAction()
                    true
                }
                R.id.toggle_view_mode_action -> {
                    presenter.toggleListAction()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

//    override fun handleBack(): Boolean {
//        Timber.d("Handling back")
//        return false
//    }
}