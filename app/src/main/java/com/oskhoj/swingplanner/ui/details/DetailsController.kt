package com.oskhoj.swingplanner.ui.details

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.widget.NestedScrollView
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_EVENT_ADD_CALENDAR_CLICK
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_EVENT_FACEBOOK_CLICK
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_EVENT_LIKE_CLICK
import com.oskhoj.swingplanner.firebase.analytics.ANALYTICS_EVENT_WEBSITE_CLICK
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.PROPERTY_IS_LIKED
import com.oskhoj.swingplanner.firebase.analytics.ScreenType
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.ui.base.ViewType
import com.oskhoj.swingplanner.util.Day
import com.oskhoj.swingplanner.util.KEY_STATE_EVENTS_DETAILS
import com.oskhoj.swingplanner.util.KEY_STATE_EVENTS_SUMMARY
import com.oskhoj.swingplanner.util.Month
import com.oskhoj.swingplanner.util.getCompatColor
import com.oskhoj.swingplanner.util.gone
import com.oskhoj.swingplanner.util.isVisible
import com.oskhoj.swingplanner.util.loadFlagIconOrDisappear
import com.oskhoj.swingplanner.util.loadImageOrDisappear
import com.oskhoj.swingplanner.util.setImageDrawable
import com.oskhoj.swingplanner.util.showTapTarget
import com.oskhoj.swingplanner.util.visible
import kotlinx.android.synthetic.main.controller_event_details.view.*
import org.jetbrains.anko.sdk21.listeners.onClick
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback
import timber.log.Timber

class DetailsController(args: Bundle = Bundle.EMPTY) :
        ToolbarController<DetailsContract.View, DetailsContract.Presenter>(args), DetailsContract.View {
    constructor(summary: EventSummary) : this() {
        eventSummary = summary
    }

    override val presenter: DetailsContract.Presenter by instance()

    override val layoutRes = R.layout.controller_event_details

    override val screenType: ScreenType = ScreenType.EVENT_DETAILS

    override val viewType: ViewType
        get() = ViewType.DETAILS_VIEW

    private lateinit var eventSummary: EventSummary

    private var eventDetails: EventDetails? = null

    private lateinit var favoriteButton: FloatingActionButton

    private lateinit var customTabsIntent: CustomTabsIntent

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<DetailsContract.Presenter>() with provider { DetailsPresenter(instance(), instance()) }
    }

    override fun eventDetailsLoaded(details: EventDetails) {
        eventDetails = details
        setupViews(view, details)
    }

    override fun displayErrorView() {
        Timber.d("Displaying error view")
        view?.load_failed_layout?.visible()
    }

    override fun openLink(url: String) {
        Timber.d("View opening link $url")
    }

    override fun addCalendarEvent(text: String) {
        Timber.d("View added calendar event")
    }

    override fun onFavoriteClicked(isSelected: Boolean) {
        view?.run {
            val fabImage = if (isSelected) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp
            favoriteButton.setImageDrawable(fabImage)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup) =
            super.onCreateView(inflater, container).apply {
                event_image.transitionName = eventSummary.imageUrl
                try_again_button.onClick { presenter.loadEventDetails(eventSummary.eventDetailsId) }
            }

    override fun onAttach(view: View) {
        super.onAttach(view)
        eventDetails?.let {
            setupViews(view, it)
        } ?: presenter.loadEventDetails(eventSummary.eventDetailsId)
    }

    private fun setupViews(view: View?, details: EventDetails) {
        view?.run {
            customTabsIntent = CustomTabsIntent.Builder()
                    .setToolbarColor(context?.getCompatColor(R.color.colorPrimary) ?: Color.WHITE)
                    .setShowTitle(true)
                    .build()
            CustomTabsHelper.addKeepAliveExtra(context, customTabsIntent.intent)

            event_image.loadImageOrDisappear(eventSummary.imageUrl, context)
            event_name.text = eventSummary.name
            load_failed_layout.gone()

            val startMonth = Month.getMonth(eventSummary.startDate.month, context)
            val startDay = Day.getDay(eventSummary.startDate, context)
            val fromField = calendar_from_text.apply {
                text = "$startDay, $startMonth ${eventSummary.dayOfMonth}"
            }
            eventSummary.endDate.takeUnless { eventSummary.isOneDayEvent() }?.run {
                fromField.append(" -")
                val endMonth = Month.getMonth(month, context)
                val endDay = Day.getDay(this, context)
                calendar_to_text.apply {
                    text = "$endDay, $endMonth $dayOfMonth"
                    visible()
                }
            } ?: calendar_to_text.gone()
            city_text.text = "${eventSummary.city},"
            if (eventSummary.country.name.isNotBlank()) {
                country_text.apply {
                    text = eventSummary.country.name
                    visible()
                }
            } else {
                country_text.gone()
            }
            country_flag.loadFlagIconOrDisappear(eventSummary.country.isoCode, context)
            dancing_text.text = details.danceStyles
            if (!details.teachersDescription.isNullOrBlank()) {
                teachers_layout.visible()
                teachers_text.text = details.teachersDescription
            }

            details.competitionsText?.let {
                competitions_layout.visible()
                competitions_text.text = it
            }
            about_description.text = details.description
            favoriteButton = favoritesFab.apply {
                val fabImage = if (AppPreferences.hasFavoriteEvent(details.id)) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp
                setImageDrawable(fabImage)
                onClick {
                    presenter.toggleFavorite(details.id)
                    AnalyticsHelper.logEvent(ANALYTICS_EVENT_LIKE_CLICK, PROPERTY_IS_LIKED to AppPreferences.hasFavoriteEvent(details.id))
                }
            }

            details.website?.takeIf { it.isNotBlank() }?.let { websiteUrl ->
                website_link.apply {
                    visible()
                    onClick {
                        AnalyticsHelper.logEvent(ANALYTICS_EVENT_WEBSITE_CLICK)
                        val website: String = websiteUrl.takeIf { websiteUrl.startsWith("http://", true) }
                                ?: "http://$websiteUrl"
                        openCustomTab(context, website)
                    }
                }
            }

            details.facebookEventUrl?.let { facebookUrl ->
                facebook_link.apply {
                    visible()
                    onClick {
                        AnalyticsHelper.logEvent(ANALYTICS_EVENT_FACEBOOK_CLICK)
                        openCustomTab(context, facebookUrl)
                    }
                }
            }

            add_to_calendar_button.onClick { addToCalendar(eventSummary) }

            scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, newScrollY, _, oldScrollY ->
                when {
                    newScrollY > oldScrollY -> favoriteButton.hide()
                    else -> favoriteButton.show()
                }
            })
            showOnboarding()
        }
    }

    private fun View.showOnboarding() {
        when {
            !AppPreferences.hasShownLikeEventTapTarget -> {
                activity?.showTapTarget(R.id.favoritesFab, R.string.tap_target_like_event_title, R.string.tap_target_like_event_message)
                AppPreferences.hasShownLikeEventTapTarget = true
            }
            !AppPreferences.hasShownWebsiteTapTarget && website_link.isVisible() -> {
                activity?.showTapTarget(R.id.website_link, R.string.tap_target_event_website_title, R.string.tap_target_event_website_message)
                AppPreferences.hasShownWebsiteTapTarget = true
            }
            !AppPreferences.hasShownCalendarTapTarget -> {
                activity?.showTapTarget(R.id.add_to_calendar_button, R.string.tap_target_add_to_calendar_title, R.string.tap_target_add_to_calendar_message)
                AppPreferences.hasShownCalendarTapTarget = true
            }
        }
    }

    private fun openCustomTab(context: Context?, url: String) {
        context?.let {
            CustomTabsHelper.openCustomTab(it, customTabsIntent, Uri.parse(url), WebViewFallback())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Timber.d("onSaveInstanceState, saving $eventSummary and $eventDetails")
        outState.putParcelable(KEY_STATE_EVENTS_SUMMARY, eventSummary)
        outState.putParcelable(KEY_STATE_EVENTS_DETAILS, eventDetails)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        eventSummary = savedInstanceState.getParcelable(KEY_STATE_EVENTS_SUMMARY) as EventSummary
        eventDetails = savedInstanceState.getParcelable(KEY_STATE_EVENTS_DETAILS) as EventDetails?
        Timber.d("Restored $eventSummary and $eventDetails")
    }

    private fun addToCalendar(eventSummary: EventSummary) {
        AnalyticsHelper.logEvent(ANALYTICS_EVENT_ADD_CALENDAR_CLICK)
        val beginTime = eventSummary.startDate.toCalendar()
        val endTime = eventSummary.endDate.toCalendar()

        val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.timeInMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.timeInMillis)
            putExtra(CalendarContract.Events.TITLE, eventSummary.name)
            putExtra(CalendarContract.Events.ALL_DAY, true)
            putExtra(CalendarContract.Events.EVENT_LOCATION, "${eventSummary.city}, ${eventSummary.country.name}")
        }
        startActivity(calendarIntent)
    }
}
