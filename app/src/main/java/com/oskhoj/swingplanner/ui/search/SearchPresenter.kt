package com.oskhoj.swingplanner.ui.search

import com.nytimes.android.external.store3.base.impl.BarCode
import com.nytimes.android.external.store3.base.impl.Store
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.model.EventsPage
import com.oskhoj.swingplanner.model.FavoritesResponse
import com.oskhoj.swingplanner.network.EventSearchBarcode
import com.oskhoj.swingplanner.network.EventSearchParams
import com.oskhoj.swingplanner.ui.base.BasePresenter
import com.oskhoj.swingplanner.util.EVENTS_PAGE
import com.oskhoj.swingplanner.util.SINGLE_EVENT
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class SearchPresenter(private val eventSummariesStore: Store<EventsPage, EventSearchBarcode>,
                      private val eventSummaryStore: Store<FavoritesResponse, BarCode>) : BasePresenter<SearchContract.View>(), SearchContract.Presenter {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun searchEvents(eventSearchParams: EventSearchParams) {
        Timber.d("Searching for $eventSearchParams")

        eventSummariesStore.get(EventSearchBarcode(EVENTS_PAGE, eventSearchParams))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<EventsPage> {
                    override fun onSubscribe(disposable: Disposable) {
                        compositeDisposable.add(disposable)
                        view?.showLoading()
                    }

                    override fun onSuccess(eventsPage: EventsPage) {
                        Timber.d("Request succeeded, got [${eventsPage.events}] events")
                        view?.hideLoading()
                        if (eventsPage.hasNoEvents()) {
                            view?.displayEmptyView()
                        } else {
                            view?.displayEvents(eventsPage)
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        Timber.w(throwable, "Request failed")
                        view?.hideLoading()
                        view?.displayErrorView()
                    }
                })
    }

    override fun openDeepLinkEvent(eventId: Int) {
        eventSummaryStore.fetch(BarCode(SINGLE_EVENT, eventId.toString()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<FavoritesResponse> {
                    override fun onSubscribe(disposable: Disposable) {
                        compositeDisposable.add(disposable)
                        view?.showLoading()
                    }

                    override fun onSuccess(favoritesResponse: FavoritesResponse) {
                        Timber.d("Request succeeded, got [${favoritesResponse.events}] events")
                        view?.hideLoading()
                        if (favoritesResponse.events.isNotEmpty()) {
                            Timber.d("Loading event...")
                            view?.openEvent(favoritesResponse.events.first())
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        Timber.w(throwable, "Request failed")
                        view?.hideLoading()
                        view?.displayErrorView()
                    }
                })
    }

    override fun onSearchBack() {
        view?.abortSearch()
    }

    override fun onSearchClear() {
        view?.clearText()
        searchEvents()
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