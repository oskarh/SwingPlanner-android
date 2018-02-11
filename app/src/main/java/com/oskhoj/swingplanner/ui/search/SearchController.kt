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
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.EventsPage
import com.oskhoj.swingplanner.network.EventSearchParams
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.base.ViewType
import com.oskhoj.swingplanner.ui.base.ViewType.SEARCH_VIEW
import com.oskhoj.swingplanner.ui.component.BottomSheetDialogHelper
import com.oskhoj.swingplanner.ui.component.HeaderEventAdapter
import com.oskhoj.swingplanner.ui.details.DetailsController
import com.oskhoj.swingplanner.util.ANALYTICS_SEARCH_EMPTY
import com.oskhoj.swingplanner.util.ANALYTICS_SEARCH_FAIL
import com.oskhoj.swingplanner.util.ANALYTICS_SEARCH_FILTER_CLICK
import com.oskhoj.swingplanner.util.ANALYTICS_SEARCH_SUCCESS
import com.oskhoj.swingplanner.util.ANALYTICS_TOGGLE_VIEW_TYPE_CLICK
import com.oskhoj.swingplanner.util.DanceStyle
import com.oskhoj.swingplanner.util.KEY_STATE_EVENTS_LIST
import com.oskhoj.swingplanner.util.KEY_STATE_LIST_POSITION
import com.oskhoj.swingplanner.util.KEY_STATE_SEARCH_TEXT
import com.oskhoj.swingplanner.util.PROPERTY_FILTERED_DANCE_STYLES
import com.oskhoj.swingplanner.util.PROPERTY_IS_CARD_VIEW
import com.oskhoj.swingplanner.util.animateToGone
import com.oskhoj.swingplanner.util.animateToVisible
import com.oskhoj.swingplanner.util.closeKeyboard
import com.oskhoj.swingplanner.util.gone
import com.oskhoj.swingplanner.util.invisible
import com.oskhoj.swingplanner.util.loadImage
import com.oskhoj.swingplanner.util.loadLayoutAnimation
import com.oskhoj.swingplanner.util.removeClickListener
import com.oskhoj.swingplanner.util.showTapTarget
import com.oskhoj.swingplanner.util.visible
import com.oskhoj.swingplanner.util.visibleIf
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controller_search.*
import kotlinx.android.synthetic.main.controller_search.view.*
import org.jetbrains.anko.sdk21.listeners.onClick
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SearchController(args: Bundle = Bundle.EMPTY) : ToolbarController<SearchContract.View, SearchContract.Presenter>(args), SearchContract.View {
    override val presenter: SearchContract.Presenter by instance()

    override val isFilterItemVisible = true
    override val isToggleViewItemVisible = true

    override val layoutRes = R.layout.controller_search

    override val viewType: ViewType = SEARCH_VIEW

    override val screenType: ScreenType = ScreenType.SEARCH

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
        Timber.d("Comparing ${searchEventsPage?.toShortString()} and ${searchPage.toShortString()}")
        AnalyticsHelper.logEvent(ANALYTICS_SEARCH_SUCCESS)
        if (searchEventsPage?.isSameSearchNextPage(searchPage) == true) {
            Timber.d("Next page of existing search")
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
        AnalyticsHelper.logEvent(ANALYTICS_TOGGLE_VIEW_TYPE_CLICK, PROPERTY_IS_CARD_VIEW to isCardView)
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
        AnalyticsHelper.logEvent(ANALYTICS_SEARCH_EMPTY)
        showEmptyErrorView(true)
    }

    override fun displayErrorView() {
        Timber.d("Displaying error view...")
        AnalyticsHelper.logEvent(ANALYTICS_SEARCH_FAIL)
        showEmptyErrorView(false)
    }

    private fun showEmptyErrorView(isEmptyView: Boolean) {
        view?.run {
            search_empty_error_text?.run {
                text = if (isEmptyView) context.getString(R.string.no_events_found) else context.getString(R.string.failed_get_events)
                visible()
            }
            search_empty_error_image?.visible()
            val drawable = if (isEmptyView) R.drawable.empty_image else R.drawable.error_image
            search_empty_error_image?.loadImage(drawable, context)
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
        view?.run {
            BottomSheetDialogHelper.showDanceFilterDialog(context) {
                searchEventsPage?.run {
                    if (stylesFilterSet != AppPreferences.filterOptions) {
                        AnalyticsHelper.logEvent(ANALYTICS_SEARCH_FILTER_CLICK,
                                PROPERTY_FILTERED_DANCE_STYLES to AppPreferences.filterOptions.joinToString())
                        searchEvents(searchText?.text?.toString())
                    }
                }
            }
        }
    }

    private fun searchEvents(query: String?, styles: Set<DanceStyle> = AppPreferences.filterOptions, page: Int = 0) {
        presenter.searchEvents(EventSearchParams(query.orEmpty(), styles, page))
    }

    override fun showLoading() {
        view?.search_progressbar?.animateToVisible()
    }

    override fun hideLoading() {
        view?.search_progressbar?.animateToGone()
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
        view.run {
            if (searchEventsPage?.hasNoEvents() == true) {
                Timber.d("Has no events")
                showEmptyErrorView(true)
            } else {
                Timber.d("Has events")
                hideEmptyErrorView()
            }
        }
        setUpRecyclerView(view)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        Timber.d("Attaching view...")
        activity?.run {
            recyclerView = search_events_recycler
            backIcon = search_back.apply {
                onClick { presenter.onSearchBack() }
            }
            clearIcon = search_clear.apply {
                onClick { presenter.onSearchClear() }
            }

            searchText = search_text?.apply {
                setText(storedText)
                setSelection(storedText.length)
                setOnFocusChangeListener { _, hasFocus ->
                    backIcon.visibleIf { hasFocus }
                    clearIcon.visibleIf { hasFocus && text.isNotBlank() }
                }
                if (searchEventsPage == null) {
                    searchEvents(storedText)
                }
                disposable = RxTextView.textChanges(this)
                        .skip(1)
                        .map { charSequence ->
                            clearIcon.visibleIf { charSequence.isNotEmpty() }
                            charSequence.trim().toString()
                        }
                        .filter { query -> query.length > 3 }
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { query -> searchEvents(query) }
            }
            if (!AppPreferences.hasShownSearchEventsTapTarget) {
                showTapTarget(R.id.search_text, R.string.tap_target_search_events_title, R.string.tap_target_search_events_message)
                AppPreferences.hasShownSearchEventsTapTarget = true
            }
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        backIcon.removeClickListener()
        clearIcon.removeClickListener()
        disposable?.dispose()
        searchText?.onFocusChangeListener = null
    }

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