package com.oskhoj.swingplanner.ui.details

import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView

object DetailsContract {

    interface View : BaseView {
        fun eventDetailsLoaded(eventDetails: EventDetails)

        fun displayErrorView()

        fun onFavoriteClicked(isSelected: Boolean)

        fun openLink(url: String)

        fun addCalendarEvent(text: String)
    }

    interface Presenter : Attachable<View> {
        fun loadEventDetails(eventId: Int)

        fun toggleFavorite(eventId: Int)

        fun onFollowClicked(eventId: Int)

        fun onAddCalendarEventClicked(eventId: Int)

        fun onFacebookClicked(url: String)

        fun onEventLinkClicked(url: String)
    }
}