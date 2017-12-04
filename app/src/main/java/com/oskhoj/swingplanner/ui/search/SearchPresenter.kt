package com.oskhoj.swingplanner.ui.search

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

class SearchPresenter(private val eventsApiManager: EventApiManager) : BasePresenter<SearchContract.View>(), SearchContract.Presenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun searchEvents(query: CharSequence) {
        Timber.d("Searching for $query")
        compositeDisposable.add(eventsApiManager.allEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith<DisposableSingleObserver<List<EventSummary>>>(object : DisposableSingleObserver<List<EventSummary>>() {
                    override fun onSuccess(events: List<EventSummary>) {
                        Timber.d("Request succeeded, got ${events.size} events")
                        view?.displayEvents(events)
                    }

                    override fun onError(error: Throwable) {
                        Timber.w(error, "Request failed")
                        view?.displayErrorView()
                    }
                }))
    }

    override fun onEventClicked(eventSummary: EventSummary) {
        Timber.d("Got event click for id ${eventSummary.id}")
        compositeDisposable.add(eventsApiManager.eventDetailsById(eventSummary.eventDetailsId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith<DisposableSingleObserver<EventDetails>>(object : DisposableSingleObserver<EventDetails>() {
                    override fun onSuccess(eventDetails: EventDetails) {
                        Timber.d("Request succeeded, got $eventDetails events")
                        view?.openEventDetails(eventSummary, eventDetails)
                    }

                    override fun onError(error: Throwable) {
                        Timber.w(error, "Request failed")
                        view?.displayErrorView()
                    }
                }))
    }

    override fun onSearchBack() {
        view?.abortSearch()
    }

    override fun onSearchClear() {
        view?.clearText()
    }

    override fun toggleListAction() {
        AppPreferences.isShowingCardList = !AppPreferences.isShowingCardList
        view?.toggleViewMode(AppPreferences.isShowingCardList)
    }

    override fun filterAction() {
        Timber.d("Filtering clicked")
        view?.showFilterDialog()
    }

    override fun aboutAction() {
        Timber.d("About clicked")
    }

    override fun onDetach() {
        super.onDetach()
        compositeDisposable.clear()
    }
}