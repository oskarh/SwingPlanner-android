package com.oskhoj.swingplanner.ui.details

import com.nytimes.android.external.store3.base.impl.BarCode
import com.nytimes.android.external.store3.base.impl.Store
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.USER_PROPERTY_NUMBER_FAVORITE_EVENTS
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.network.SubscriptionApiManager
import com.oskhoj.swingplanner.ui.base.BasePresenter
import com.oskhoj.swingplanner.util.EVENT_DETAILS
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class DetailsPresenter(private val eventDetailsStore: Store<EventDetails, BarCode>, private val subscriptionApiManager: SubscriptionApiManager) :
        BasePresenter<DetailsContract.View>(), DetailsContract.Presenter {

    override fun loadEventDetails(eventId: Int) {
        Timber.d("Loading event $eventId")
        eventDetailsStore.get(BarCode(EVENT_DETAILS, eventId.toString()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<EventDetails> {
                    override fun onSubscribe(disposable: Disposable) {
                        Timber.d("onSubscribe...")
                    }

                    override fun onSuccess(eventDetails: EventDetails) {
                        Timber.d("Request succeeded, got $eventDetails events")
                        view?.eventDetailsLoaded(eventDetails)
                    }

                    override fun onError(error: Throwable) {
                        Timber.w(error, "Request failed")
                        view?.displayErrorView()
                    }
                })
    }

    override fun toggleFavorite(eventId: Int) {
        val isAddedFavorite = AppPreferences.toggleFavoriteEvent(eventId)
        if (isAddedFavorite) {
            subscriptionApiManager.addEventSubscription(eventId)
        } else {
            subscriptionApiManager.removeEventSubscription(eventId)
        }
        AnalyticsHelper.setUserProperty(USER_PROPERTY_NUMBER_FAVORITE_EVENTS, AppPreferences.numberFavoriteEvents())
        view?.onFavoriteClicked(isAddedFavorite)
    }

    override fun onFollowClicked(eventId: Int) {
        Timber.d("Now following id $eventId")
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