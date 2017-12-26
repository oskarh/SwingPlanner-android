package com.oskhoj.swingplanner.ui.details

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.AppCompatImageView
import android.view.MenuItem
import android.view.View
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.ViewType
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.ui.base.ToolbarController
import com.oskhoj.swingplanner.util.Day
import com.oskhoj.swingplanner.util.KEY_STATE_EVENTS_DETAILS
import com.oskhoj.swingplanner.util.KEY_STATE_EVENTS_SUMMARY
import com.oskhoj.swingplanner.util.Month
import com.oskhoj.swingplanner.util.getCompatColor
import com.oskhoj.swingplanner.util.gone
import com.oskhoj.swingplanner.util.loadFlagIconOrDisappear
import com.oskhoj.swingplanner.util.loadImageOrDisappear
import com.oskhoj.swingplanner.util.visible
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controller_event_details.view.*
import org.jetbrains.anko.sdk21.listeners.onClick
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback
import timber.log.Timber

class DetailsController(args: Bundle = Bundle.EMPTY) :
        ToolbarController<DetailsContract.View, DetailsContract.Presenter>(args), DetailsContract.View {

    constructor(summary: EventSummary, details: EventDetails) : this() {
        eventSummary = summary
        eventDetails = details
    }

    override val presenter: DetailsContract.Presenter by instance()

    override val layoutRes = R.layout.controller_event_details

    override val viewType: ViewType
        get() = ViewType.DETAILS_VIEW

    private lateinit var eventSummary: EventSummary

    private lateinit var eventDetails: EventDetails
    override val isShareItemVisible = true

    override val isAddToCalendarItemVisible = true
    private lateinit var favoriteButton: FloatingActionButton

    private lateinit var customTabsIntent: CustomTabsIntent

    override val controllerModule = Kodein.Module(allowSilentOverride = true) {
        bind<DetailsContract.Presenter>() with instance(DetailsPresenter())
    }

    override fun openLink(url: String) {
        Timber.d("View opening link $url")
    }

    override fun addCalendarEvent(text: String) {
        Timber.d("View added calendar event")
    }

    private fun enableCollapsingToolbar(isEnabled: Boolean) {
        activity?.run {
            val params = collapsing_toolbar.layoutParams as AppBarLayout.LayoutParams
            if (isEnabled) {
                params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
            } else {
                params.scrollFlags = 0
            }
        }
    }

    override fun onFavoriteClicked(isSelected: Boolean) {
        view?.run {
            val fabImage = if (isSelected) R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp
            favoriteButton.setImageDrawable(ContextCompat.getDrawable(context, fabImage))
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        view.run {
            customTabsIntent = CustomTabsIntent.Builder()
                    .setToolbarColor(context?.getCompatColor(R.color.colorPrimary) ?: Color.WHITE)
                    .setShowTitle(true)
                    .build()
            CustomTabsHelper.addKeepAliveExtra(context, customTabsIntent.intent)
            //            enableCollapsingToolbar(true)
//            val mAppBarLayout = context.findViewById<AppBarLayout>(R.id.app_bar_layout)
//            mAppBarLayout.setExpanded(true)

//            eventSummary.imageUrl?.let {
//                val toolbarImage = context.findViewById<AppCompatImageView>(R.id.toolbar_image)
//                toolbarImage.loadImage(it, context)
//                toolbarImage.visible()
//            }

            event_image.loadImageOrDisappear(eventSummary.imageUrl, context)
            event_name.text = eventSummary.name

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
            }
            city_text.text = "${eventSummary.city},"
            if (eventSummary.country.name.isNotBlank()) {
                country_text.apply {
                    text = eventSummary.country.name
                    visible()
                }
            }
            country_flag.loadFlagIconOrDisappear(eventSummary.country.isoCode, context)
            dancing_text.text = eventDetails.danceStyles
            if (!eventDetails.teachersDescription.isNullOrBlank()) {
                teachers_layout.visible()
                teachers_text.text = eventDetails.teachersDescription
            }

            eventDetails.competitionsText?.let {
                competitions_layout.visible()
                competitions_text.text = it
            }
            about_description.text = eventDetails.description
            favoriteButton = favoritesFab.apply {
                val fabImage = if (AppPreferences.hasFavoriteEvent(eventDetails.id)) R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp
                setImageDrawable(ContextCompat.getDrawable(context, fabImage))
                onClick { presenter.toggleFavorite(eventDetails.id) }
            }

            eventDetails.facebookEventUrl?.let { facebookUrl ->
                facebook_link.apply {
                    visible()
                    onClick {
                        openCustomTab(context, facebookUrl)
                    }
                }
            }

            eventDetails.website?.takeIf { it.isNotBlank() }?.let { websiteUrl ->
                website_link.apply {
                    visible()
                    setOnClickListener {
                        val website: String = websiteUrl.takeIf { websiteUrl.startsWith("http://", true) } ?: "http://$websiteUrl"
                        openCustomTab(context, website)
                    }
                }
            }

            scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, newScrollY, _, oldScrollY ->
                when {
                    newScrollY > oldScrollY -> favoriteButton.hide()
                    else -> favoriteButton.show()
                }
            })
        }
    }

    private fun openCustomTab(context: Context?, url: String) {
        context?.let {
            CustomTabsHelper.openCustomTab(it, customTabsIntent, Uri.parse(url), WebViewFallback())
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        activity?.let { context ->
            val appBarLayout = context.findViewById<AppBarLayout>(R.id.app_bar_layout)
            enableCollapsingToolbar(false)
            val mAppBarLayout = context.findViewById<AppBarLayout>(R.id.app_bar_layout)
            mAppBarLayout.setExpanded(false)

            val toolbarImage = context.findViewById<AppCompatImageView>(R.id.toolbar_image)
            toolbarImage.gone()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_STATE_EVENTS_SUMMARY, eventSummary)
        outState.putParcelable(KEY_STATE_EVENTS_DETAILS, eventDetails)
        Timber.d("onSaveInstanceState, saving $eventSummary and $eventDetails")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        eventSummary = savedInstanceState.getParcelable<EventSummary>(KEY_STATE_EVENTS_SUMMARY) as EventSummary
        eventDetails = savedInstanceState.getParcelable<EventDetails>(KEY_STATE_EVENTS_DETAILS) as EventDetails
        Timber.d("Restored $eventSummary and $eventDetails")
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.share_action -> {
                    Timber.d("Sharing event...")
                    true
                }
                R.id.calendar_action -> {
                    Timber.d("Adding event to calendar")
                    addToCalendar(eventSummary)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    private fun addToCalendar(eventSummary: EventSummary) {
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
