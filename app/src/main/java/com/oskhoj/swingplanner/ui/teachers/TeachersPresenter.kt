package com.oskhoj.swingplanner.ui.teachers

import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.Teacher
import com.oskhoj.swingplanner.network.EventApiManager
import com.oskhoj.swingplanner.network.TeacherApiManager
import com.oskhoj.swingplanner.ui.base.BasePresenter
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class TeachersPresenter(private val eventsApiManager: EventApiManager, private val teacherApiManager: TeacherApiManager) : BasePresenter<TeachersContract.View>(), TeachersContract.Presenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun loadTeachers(query: String) {
        teacherApiManager.allTeachers()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<List<Teacher>> {
                    override fun onSubscribe(disposable: Disposable) {
                        compositeDisposable.add(disposable)
                        view?.showLoading()
                    }

                    override fun onSuccess(teachers: List<Teacher>) {
                        Timber.d("Request succeeded, got [${teachers.size}] teachers")
                        view?.hideLoading()
                        view?.displayTeachers(teachers.filter { it.name.contains(query.trim(), true) })
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
        view?.openTeacherDetails(emptyList())
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

    override fun aboutAction() {
        Timber.d("About clicked")
    }
}