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
import com.jakewharton.rxbinding2.widget.RxTextView
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.ViewType
import com.oskhoj.swingplanner.ViewType.SEARCH_VIEW
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.EventsPage
import com.oskhoj.swingplanner.network.EventSearchParams
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.component.BottomSheetDialogHelper
import com.oskhoj.swingplanner.ui.component.HeaderEventAdapter
import com.oskhoj.swingplanner.ui.details.DetailsController
import com.oskhoj.swingplanner.util.DanceStyle
import com.oskhoj.swingplanner.util.KEY_STATE_EVENTS_LIST
import com.oskhoj.swingplanner.util.KEY_STATE_LIST_POSITION
import com.oskhoj.swingplanner.util.KEY_STATE_SEARCH_TEXT
import com.oskhoj.swingplanner.util.closeKeyboard
import com.oskhoj.swingplanner.util.gone
import com.oskhoj.swingplanner.util.invisible
import com.oskhoj.swingplanner.util.loadLayoutAnimation
import com.oskhoj.swingplanner.util.visible
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controller_search.*
import kotlinx.android.synthetic.main.controller_search.view.*
import timber.log.Timber
import java.util.concurrent.TimeUnit


class SearchController(args: Bundle = Bundle.EMPTY) : ToolbarController<SearchContract.View, SearchContract.Presenter>(args), SearchContract.View {
    override val presenter: SearchContract.Presenter by instance()

    override val isFilterItemVisible = true
    override val isToggleViewItemVisible = true

    override val layoutRes = R.layout.controller_search

    override val viewType: ViewType = SEARCH_VIEW

    private var recyclerView: RecyclerView? = null
    private lateinit var backIcon: AppCompatImageView
    private var searchText: EditText? = null

    private var disposable: Disposable? = null

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<SearchContract.Presenter>() with provider { SearchPresenter(instance(), instance()) }
    }

    private lateinit var clearIcon: AppCompatImageView

    private val eventAdapter: HeaderEventAdapter = HeaderEventAdapter(emptyList(), {
        Timber.d("Clicked on event with id ${it.id}")
        presenter.onEventClicked(it)
    })

    private var listState: Parcelable? = null

    private var storedText: String = ""

    private var searchEventsPage: EventsPage? = null

    override fun displayEvents(searchPage: EventsPage) {
        Timber.d("Displaying $searchPage")
        Timber.d("Comparing ${searchEventsPage?.toShortString()} and ${searchPage.toShortString()}")
        if (searchEventsPage?.isSameSearchNextPage(searchPage) == true) {
            Timber.d("Was same search for next page")
            eventAdapter.addEvents(searchPage.events)
        } else {
            Timber.d("New search")
            eventAdapter.loadEventsPage(searchPage)
            hideEmptyErrorView()
            recyclerView?.layoutManager?.scrollToPosition(0)
        }
        searchEventsPage = searchPage
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
        showEmptyErrorView()
    }

    override fun displayErrorView() {
        Timber.d("Displaying error view...")
        showEmptyErrorView()
    }

    private fun showEmptyErrorView() {
        view?.run {
            search_empty_error_text?.visible()
            search_empty_error_image?.visible()
            search_events_recycler?.gone()
        }
    }

    private fun hideEmptyErrorView() {
        view?.run {
            search_events_recycler?.run {
                visible()
                layoutManager?.scrollToPosition(0)
            }
            search_empty_error_text?.gone()
            search_empty_error_image?.gone()
        }
    }

    override fun clearText() {
        searchText?.text?.clear()
        clearIcon.invisible()
    }

    override fun showFilterDialog() {
        BottomSheetDialogHelper.showFilterDialog(view?.context) {
            searchEventsPage?.run {
                if (stylesFilterSet != AppPreferences.filterOptions) {
                    Timber.d("Filter changed from $stylesFilterSet to ${AppPreferences.filterOptions}, searching again")
                    searchEvents(searchText?.text?.toString())
                } else {
                    Timber.d("Filter styles is the same")
                }
            }
        }
    }

    private fun searchEvents(query: String?, styles: Set<DanceStyle> = AppPreferences.filterOptions, page: Int = 0) {
        presenter.searchEvents(EventSearchParams(query ?: "", styles, page))
    }

    override fun showLoading() {
        view?.search_progressbar?.visible()
    }

    override fun hideLoading() {
        view?.search_progressbar?.gone()
    }

    private fun updateMenuItemIcon(isCardView: Boolean) {
        activity?.let {
            val toggleIcon = if (isCardView) R.drawable.ic_view_list_black_24dp else R.drawable.ic_view_module_black_24dp
            menu.findItem(R.id.toggle_view_mode_action).run {
                icon = ContextCompat.getDrawable(it, toggleIcon)
            }
        }
    }

    private fun setUpRecyclerView(view: View) {
        with(view.search_events_recycler) {
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
                        searchEventsPage?.run {
                            if (!isLastPage) {
                                Timber.d("Found another page to load, loading page ${pageNumber + 1}")
                                searchEvents(query, stylesFilterSet, pageNumber + 1)
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        Timber.d("onViewBound")
        view.run {
            if (searchEventsPage?.hasNoEvents() == true) {
                Timber.d("Has no events")
                showEmptyErrorView()
            } else {
                Timber.d("Has events")
                hideEmptyErrorView()
            }
        }
        setUpRecyclerView(view)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        Timber.d("Attaching view..")
        activity?.run {
            recyclerView = search_events_recycler
            backIcon = search_back
            backIcon.setOnClickListener { presenter.onSearchBack() }
            clearIcon = search_clear
            clearIcon.setOnClickListener { presenter.onSearchClear() }

            searchText = search_text?.apply {
                setText(storedText)
                setSelection(storedText.length)
                setOnFocusChangeListener { _, hasFocus ->
                    backIcon.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
                    clearIcon.visibility = if (hasFocus && text.isNotBlank()) View.VISIBLE else View.INVISIBLE
                }
                if (searchEventsPage == null) {
                    searchEvents(storedText)
                }
                disposable = RxTextView.textChanges(this)
                        .skip(1)
                        .map { charSequence -> charSequence.trim().toString() }
                        .filter { query -> query.length > 3 }
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { query ->
                            Timber.d("Debounced $query")
                            clearIcon.visibility = if (query.isEmpty()) View.INVISIBLE else View.VISIBLE
                            searchEvents(query)
                        }
            }
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        backIcon.setOnClickListener(null)
        clearIcon.setOnClickListener(null)
        disposable?.dispose()
        searchText?.onFocusChangeListener = null
    }

    // TODO: Check if searchText has been initialized
    override fun onSaveInstanceState(outState: Bundle) {
        Timber.d("Saving $searchEventsPage")
        outState.putParcelable(KEY_STATE_EVENTS_LIST, searchEventsPage)
        outState.putString(KEY_STATE_SEARCH_TEXT, searchText?.text?.toString() ?: "")
        outState.putParcelable(KEY_STATE_LIST_POSITION, recyclerView?.layoutManager?.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        storedText = savedInstanceState.getString(KEY_STATE_SEARCH_TEXT)
        searchEventsPage = savedInstanceState.getParcelable<EventsPage>(KEY_STATE_EVENTS_LIST)?.apply { displayEvents(this) }
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