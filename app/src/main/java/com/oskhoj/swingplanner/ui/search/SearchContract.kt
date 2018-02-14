package com.oskhoj.swingplanner.ui.search

import com.oskhoj.swingplanner.model.EventsPage
import com.oskhoj.swingplanner.network.EventSearchParams
import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView
import com.oskhoj.swingplanner.ui.base.Loadable

object SearchContract {

    interface View : BaseView, Loadable {
        fun displayEvents(searchPage: EventsPage)

        fun toggleViewMode(isCardView: Boolean)

        fun abortSearch()

        fun clearText()

        fun displayEmptyView()

        fun displayErrorView()

        fun showFilterDialog()
    }

    interface Presenter : Attachable<View> {
        fun searchEvents(eventSearchParams: EventSearchParams = EventSearchParams())

        fun onSearchBack()

        fun onSearchClear()

        fun filterAction()

        fun toggleListAction()

        fun aboutAction()
    }
}