package com.oskhoj.swingplanner.ui.favorites

import com.nytimes.android.external.store3.base.impl.Store
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.model.FavoritesResponse
import com.oskhoj.swingplanner.network.FavoritesBarcode
import com.oskhoj.swingplanner.network.FavoritesParameters
import com.oskhoj.swingplanner.ui.base.BasePresenter
import com.oskhoj.swingplanner.util.FAVORITES_PAGE
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class FavoritesPresenter(private val listEventStore: Store<FavoritesResponse, FavoritesBarcode>)
    : BasePresenter<FavoritesContract.View>(), FavoritesContract.Presenter {

    override fun loadFavorites() {
        val favoriteIds = AppPreferences.favoriteEventIds.toList()
        if (favoriteIds.isEmpty()) {
            Timber.d("No favorite ids to show...")
            view?.displayEmptyView()
        } else {
            listEventStore.get(FavoritesBarcode(FAVORITES_PAGE, FavoritesParameters(favoriteIds)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { view?.showLoading() }
                    .subscribe(object : DisposableSingleObserver<FavoritesResponse>() {
                        override fun onSuccess(favoritesResponse: FavoritesResponse) {
                            Timber.d("Request succeeded, got ${favoritesResponse.events.size} events")
                            view?.hideLoading()
                            when {
                                favoritesResponse.events.isEmpty() -> view?.displayEmptyView()
                                else -> view?.displayEvents(favoritesResponse.events)
                            }
                        }

                        override fun onError(error: Throwable) {
                            Timber.w(error, "Request failed")
                            view?.hideLoading()
                            view?.displayErrorView()
                        }
                    })
        }
    }

    override fun toggleListAction() {
        AppPreferences.isShowingCardList = !AppPreferences.isShowingCardList
        view?.toggleViewMode(AppPreferences.isShowingCardList)
    }

    override fun aboutAction() {
        Timber.d("About clicked")
    }
}