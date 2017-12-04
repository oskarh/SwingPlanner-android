package com.oskhoj.swingplanner.ui.favorites

import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.network.EventApiManager
import com.oskhoj.swingplanner.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class FavoritesPresenter(private val eventsApiManager: EventApiManager) : BasePresenter<FavoritesContract.View>(), FavoritesContract.Presenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun loadFavorites() {
        val favoriteIds = AppPreferences.favoriteEventIds.toList()
        if (favoriteIds.isEmpty()) {
            Timber.d("No favorite ids to show...")
            view?.displayEmptyView()
        } else {
            val subscribeWith: DisposableSingleObserver<List<EventSummary>> = eventsApiManager.eventsByIds(favoriteIds)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribeWith(object : DisposableSingleObserver<List<EventSummary>>() {
                        override fun onSuccess(events: List<EventSummary>) {
                            Timber.d("Request succeeded, got ${events.size} events")
                            view?.displayEvents(events)
                        }

                        override fun onError(error: Throwable) {
                            Timber.w(error, "Request failed")
                            view?.displayErrorView()
                        }
                    })
            compositeDisposable.add(subscribeWith)
        }
    }

    override fun toggleListAction() {
        AppPreferences.isShowingCardList = !AppPreferences.isShowingCardList
        view?.toggleViewMode(AppPreferences.isShowingCardList)
    }

    override fun onEventClicked(eventSummary: EventSummary) {
        Timber.d("Got event click for id ${eventSummary.id}")
        val subscribeWith: DisposableSingleObserver<EventDetails> = eventsApiManager.eventDetailsById(eventSummary.eventDetailsId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableSingleObserver<EventDetails>() {
                    override fun onSuccess(eventDetails: EventDetails) {
                        Timber.d("Request succeeded, got $eventDetails events")
                        view?.openEventDetails(eventSummary, eventDetails)
                    }

                    override fun onError(error: Throwable) {
                        Timber.w(error, "Request failed")
                        view?.displayErrorView()
                    }
                })
        compositeDisposable.add(subscribeWith)
    }

    override fun aboutAction() {
        Timber.d("About clicked")
    }
}