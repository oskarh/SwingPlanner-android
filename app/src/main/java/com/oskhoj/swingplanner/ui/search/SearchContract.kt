package com.oskhoj.swingplanner.ui.search

import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView

object SearchContract {

    interface View : BaseView {
        fun displayEvents(events: List<EventSummary>)

        fun toggleViewMode(isCardView: Boolean)

        fun abortSearch()

        fun clearText()

        fun openEventDetails(eventSummary: EventSummary, eventDetails: EventDetails)

        fun displayErrorView()

        fun showFilterDialog()
    }

    interface Presenter : Attachable<View> {
        fun searchEvents(query: CharSequence = "")

        fun onSearchBack()

        fun onSearchClear()

        fun filterAction()

        fun toggleListAction()

        fun aboutAction()

        fun onEventClicked(eventSummary: EventSummary)
    }
}