package com.oskhoj.swingplanner.ui.teachers

import com.nytimes.android.external.store3.base.impl.BarCode
import com.nytimes.android.external.store3.base.impl.Store
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.Teacher
import com.oskhoj.swingplanner.model.TeacherEventsResponse
import com.oskhoj.swingplanner.model.TeachersResponse
import com.oskhoj.swingplanner.ui.base.BasePresenter
import com.oskhoj.swingplanner.util.EVENT_DETAILS
import com.oskhoj.swingplanner.util.TEACHER
import com.oskhoj.swingplanner.util.TEACHER_EVENTS
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class TeachersPresenter(private val store: Store<TeachersResponse, BarCode>,
                        private val teacherEventStore: Store<TeacherEventsResponse, BarCode>,
                        private val eventDetailsStore: Store<EventDetails, BarCode>) :
        BasePresenter<TeachersContract.View>(), TeachersContract.Presenter {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun loadTeachers(query: String) {
        store.get(BarCode(TEACHER, ""))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<TeachersResponse> {
                    override fun onSubscribe(disposable: Disposable) {
                        compositeDisposable.add(disposable)
                        view?.showLoading()
                    }

                    override fun onSuccess(response: TeachersResponse) {
                        Timber.d("Request succeeded, got [${response.teachers.size}] teachers")
                        view?.hideLoading()
                        view?.displayTeachers(response.teachers.filter { it.name.contains(query.trim(), true) })
                    }

                    override fun onError(throwable: Throwable) {
                        Timber.w(throwable, "Request failed")
                        view?.hideLoading()
                        view?.displayErrorView()
                    }
                })
    }

    override fun openTeacherDetails(teacher: Teacher) {
        Timber.d("Opening ${teacher.name} details")
        teacherEventStore.get(BarCode(TEACHER_EVENTS, teacher.id.toString()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<TeacherEventsResponse> {
                    override fun onSubscribe(disposable: Disposable) {
                        compositeDisposable.add(disposable)
                        view?.showLoading()
                    }

                    override fun onSuccess(response: TeacherEventsResponse) {
                        Timber.d("Request succeeded, got [$response]")
                        view?.hideLoading()
                        view?.displayTeacherEvents(response)
                    }

                    override fun onError(throwable: Throwable) {
                        Timber.w(throwable, "Request failed ${throwable.message}")
                        view?.hideLoading()
                        view?.displayErrorView()
                    }
                })
    }

    override fun toggleTeacherLike(teacher: Teacher) {
        AppPreferences.toggleFavoriteTeacher(teacher.id)
        view?.onFavoriteClicked(AppPreferences.hasFavoriteTeacher(teacher.id))
    }

    override fun findTeacherOnYouTube(teacher: Teacher) {
        Timber.d("Searching for ${teacher.name} on YouTube")
    }

    override fun openEventDetails(eventSummary: EventSummary) {
        Timber.d("Got event click for id ${eventSummary.id}")
        eventDetailsStore.get(BarCode(EVENT_DETAILS, eventSummary.id.toString()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : DisposableSingleObserver<EventDetails>() {
                    override fun onSuccess(eventDetails: EventDetails) {
                        Timber.d("Request succeeded, got $eventDetails events")
                        view?.openEventDetails(eventSummary, eventDetails)
                    }

                    override fun onError(error: Throwable) {
                        Timber.w(error, "Request failed")
                        view?.displayErrorView()
                    }
                })
    }

    override fun onSearchBack() {
        view?.abortSearch()
    }

    override fun onSearchClear() {
        view?.clearText()
    }

    override fun aboutAction() {
        Timber.d("About clicked")
    }
}