package com.oskhoj.swingplanner.ui.details

import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView

object DetailsContract {

    interface View : BaseView {
        fun onFavoriteClicked(isSelected: Boolean)

        fun openLink(url: String)

        fun addCalendarEvent(text: String)
    }

    interface Presenter : Attachable<View> {
        fun toggleFavorite(eventId: Int)

        fun onFollowClicked(eventId: Int)

        fun onShareClicked(eventId: Int)

        fun onAddCalendarEventClicked(eventId: Int)

        fun onFacebookClicked(url: String)

        fun onEventLinkClicked(url: String)
    }
}