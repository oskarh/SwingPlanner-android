package com.oskhoj.swingplanner.ui.details

import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.ui.base.BasePresenter
import timber.log.Timber

class DetailsPresenter : BasePresenter<DetailsContract.View>(), DetailsContract.Presenter {

    override fun toggleFavorite(eventId: Int) {
        AppPreferences.toggleFavoriteEvent(eventId)
        Timber.d("Toggled id $eventId, now has favorites ${AppPreferences.favoriteEventIds}")
        view?.onFavoriteClicked(AppPreferences.hasFavoriteEvent(eventId))
    }

    override fun onFollowClicked(eventId: Int) {
        Timber.d("Now following id $eventId")
        // TODO: Subscribe to changes for event in backend
    }

    override fun onShareClicked(eventId: Int) {
        Timber.d("Shared id $eventId")
    }

    override fun onAddCalendarEventClicked(eventId: Int) {
        Timber.d("Added event $eventId to calendar")
        view?.addCalendarEvent("")
    }

    override fun onFacebookClicked(url: String) {
        Timber.d("Clicked facebook event with url $url")
        view?.openLink(url)
    }

    override fun onEventLinkClicked(url: String) {
        Timber.d("Opening $url")
        view?.openLink(url)
    }
}