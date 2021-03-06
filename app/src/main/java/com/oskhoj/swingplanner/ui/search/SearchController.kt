package com.oskhoj.swingplanner.ui.search

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jakewharton.rxbinding2.widget.RxTextView
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_OPENED_DEEP_LINK
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_SEARCH_EMPTY
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_SEARCH_FAIL
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_SEARCH_FILTER_CLICK
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_SEARCH_SUCCESS
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_TOGGLE_VIEW_TYPE_CLICK
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.PROPERTY_DEEP_LINK_EVENT_ID
import com.oskhoj.swingplanner.firebase.analytics.PROPERTY_FILTERED_DANCE_STYLES
import com.oskhoj.swingplanner.firebase.analytics.PROPERTY_IS_CARD_VIEW
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.EventsPage
import com.oskhoj.swingplanner.network.EventSearchParams
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.base.ViewType
import com.oskhoj.swingplanner.ui.base.ViewType.SEARCH_VIEW
import com.oskhoj.swingplanner.ui.component.BottomSheetDialogHelper
import com.oskhoj.swingplanner.ui.component.HeaderEventAdapter
import com.oskhoj.swingplanner.ui.component.TransitionHandler
import com.oskhoj.swingplanner.ui.component.shouldShowRatingDialog
import com.oskhoj.swingplanner.ui.component.showRatingDialog
import com.oskhoj.swingplanner.ui.details.DetailsController
import com.oskhoj.swingplanner.util.DanceStyle
import com.oskhoj.swingplanner.util.KEY_STATE_DEEP_LINK_EVENT_ID
import com.oskhoj.swingplanner.util.KEY_STATE_EVENTS_LIST
import com.oskhoj.swingplanner.util.KEY_STATE_LIST_POSITION
import com.oskhoj.swingplanner.util.KEY_STATE_SEARCH_TEXT
import com.oskhoj.swingplanner.util.NOT_SET
import com.oskhoj.swingplanner.util.REMOTE_CONFIG_SEARCH_DELAY
import com.oskhoj.swingplanner.util.USA
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
    private val searchDelay = FirebaseRemoteConfig.getInstance().getLong(REMOTE_CONFIG_SEARCH_DELAY)

    private var disposable: Disposable? = null
    private var firstVisibleItem = NOT_SET

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<SearchContract.Presenter>() with provider { SearchPresenter(instance(), instance()) }
    }

    private lateinit var clearIcon: AppCompatImageView

    private val eventAdapter: HeaderEventAdapter = HeaderEventAdapter(emptyList()) {
        Timber.d("Clicked on event with id ${it.id}")
        openEvent(it)
    }

    private var storedText: String = ""

    private var searchEventsPage: EventsPage? = null

    override fun displayEvents(searchPage: EventsPage) {
        Timber.d("Comparing ${searchEventsPage?.toShortString()} and ${searchPage.toShortString()}")
        AnalyticsHelper.logEvent(ANALYTICS_SEARCH_SUCCESS)
        if (searchEventsPage?.isSameSearchNextPage(searchPage) == true) {
            Timber.d("Next page of existing search")
            eventAdapter.addEvents(searchPage.events)
        } else {
            firstVisibleItem = NOT_SET
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

    override fun openEvent(eventSummary: EventSummary) {
        activity?.closeKeyboard()
        val transitionHandler = TransitionHandler()
        router.pushController(RouterTransaction.with(DetailsController(eventSummary))
                .pushChangeHandler(transitionHandler)
                .popChangeHandler(transitionHandler))
    }

    override fun displayEmptyView() {
        AnalyticsHelper.logEvent(ANALYTICS_SEARCH_EMPTY)
        showEmptyErrorView(true)
    }

    override fun displayErrorView() {
        AnalyticsHelper.logEvent(ANALYTICS_SEARCH_FAIL)
        showEmptyErrorView(false)
    }

    private fun showEmptyErrorView(isEmptyView: Boolean) {
        firstVisibleItem = NOT_SET
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

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy != 0) {
                        activity?.closeKeyboard()
                    }
                    if (!recyclerView.canScrollVertically(RecyclerView.VERTICAL)) {
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
                showEmptyErrorView(true)
            } else {
                hideEmptyErrorView()
            }
        }
        setUpRecyclerView(view)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        activity?.run {
            recyclerView = search_events_recycler.apply {
                if (firstVisibleItem != NOT_SET) {
                    scrollToPosition(firstVisibleItem)
                }
            }
            backIcon = search_back.apply {
                onClick { presenter.onSearchBack() }
            }
            clearIcon = search_clear.apply {
                visibleIf { searchEventsPage?.query?.isNotEmpty() == true }
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
                        .filter { query -> query.length > 3 || query == USA }
                        .debounce(searchDelay, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { query -> searchEvents(query) }
            }
            if (!AppPreferences.hasShownSearchEventsTapTarget) {
                showTapTarget(R.id.search_text, R.string.tap_target_search_events_title, R.string.tap_target_search_events_message)
                AppPreferences.hasShownSearchEventsTapTarget = true
            }
            val deepLinkedEventId = intent?.getIntExtra(KEY_STATE_DEEP_LINK_EVENT_ID, NOT_SET) ?: NOT_SET
            if (deepLinkedEventId != NOT_SET) {
                intent?.removeExtra(KEY_STATE_DEEP_LINK_EVENT_ID)
                AnalyticsHelper.logEvent(ANALYTICS_OPENED_DEEP_LINK, PROPERTY_DEEP_LINK_EVENT_ID to deepLinkedEventId)
                presenter.openDeepLinkEvent(deepLinkedEventId)
            } else if (shouldShowRatingDialog(this)) {
                showRatingDialog(this)
            }
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        firstVisibleItem = (recyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        storedText = searchText?.text?.toString().orEmpty()
        backIcon.removeClickListener()
        clearIcon.removeClickListener()
        disposable?.dispose()
        searchText?.onFocusChangeListener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_STATE_EVENTS_LIST, searchEventsPage)
        outState.putString(KEY_STATE_SEARCH_TEXT, searchText?.text?.toString() ?: "")
        outState.putInt(KEY_STATE_LIST_POSITION, firstVisibleItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        storedText = savedInstanceState.getString(KEY_STATE_SEARCH_TEXT)
        searchEventsPage = savedInstanceState.getParcelable<EventsPage>(KEY_STATE_EVENTS_LIST)?.apply { displayEvents(this) }
        firstVisibleItem = savedInstanceState.getInt(KEY_STATE_LIST_POSITION)
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