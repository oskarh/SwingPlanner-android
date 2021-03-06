package com.oskhoj.swingplanner.ui.favorites

import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView
import com.oskhoj.swingplanner.ui.base.Loadable

object FavoritesContract {

    interface View : BaseView, Loadable {
        fun displayEvents(events: List<EventSummary>)

        fun toggleViewMode(isCardView: Boolean)

        fun displayEmptyView()

        fun displayErrorView()
    }

    interface Presenter : Attachable<View> {
        fun loadFavorites()

        fun toggleListAction()

        fun aboutAction()
    }
}
