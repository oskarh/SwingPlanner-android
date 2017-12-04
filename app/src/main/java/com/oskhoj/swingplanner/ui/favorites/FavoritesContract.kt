package com.oskhoj.swingplanner.ui.favorites

import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView

object FavoritesContract {

    interface View : BaseView {
        fun displayEvents(events: List<EventSummary>)

        fun toggleViewMode(isCardView: Boolean)

        fun openEventDetails(eventSummary: EventSummary, eventDetails: EventDetails)

        fun displayEmptyView()

        fun displayErrorView()
    }

    interface Presenter : Attachable<View> {
        fun loadFavorites()

        fun toggleListAction()

        fun aboutAction()

        fun onEventClicked(eventSummary: EventSummary)
    }
}
