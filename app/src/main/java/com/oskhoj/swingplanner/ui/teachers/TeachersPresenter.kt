package com.oskhoj.swingplanner.ui.teachers

import com.nytimes.android.external.store3.base.impl.BarCode
import com.nytimes.android.external.store3.base.impl.Store
import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.firebase.analytics.USER_PROPERTY_NUMBER_FAVORITE_TEACHERS
import com.oskhoj.swingplanner.model.Teacher
import com.oskhoj.swingplanner.model.TeacherEventsResponse
import com.oskhoj.swingplanner.model.TeachersResponse
import com.oskhoj.swingplanner.network.SubscriptionApiManager
import com.oskhoj.swingplanner.ui.base.BasePresenter
import com.oskhoj.swingplanner.util.TEACHER
import com.oskhoj.swingplanner.util.TEACHER_EVENTS
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class TeachersPresenter(private val store: Store<TeachersResponse, BarCode>,
                        private val teacherEventStore: Store<TeacherEventsResponse, BarCode>,
                        private val subscriptionApiManager: SubscriptionApiManager) :
        BasePresenter<TeachersContract.View>(), TeachersContract.Presenter {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun loadTeachers(query: String) {
        store.get(BarCode(TEACHER, ""))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<TeachersResponse> {
                    override fun onSubscribe(disposable: Disposable) {
                        compositeDisposable.add(disposable)
                    }

                    override fun onSuccess(response: TeachersResponse) {
                        Timber.d("Request succeeded, got [${response.teachers.size}] teachers")
                        view?.displayTeachers(response.teachers.filter { it.name.contains(query.trim(), true) })
                    }

                    override fun onError(throwable: Throwable) {
                        Timber.w(throwable, "Request failed")
                        view?.displayErrorView()
                    }
                })
    }

    override fun openTeacherDetails(teacherId: Int) {
        teacherEventStore.get(BarCode(TEACHER_EVENTS, teacherId.toString()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<TeacherEventsResponse> {
                    override fun onSubscribe(disposable: Disposable) {
                        compositeDisposable.add(disposable)
                    }

                    override fun onSuccess(response: TeacherEventsResponse) {
                        Timber.d("Request succeeded, got [$response]")
                        view?.displayTeacherEvents(response)
                    }

                    override fun onError(throwable: Throwable) {
                        Timber.w(throwable, "Request failed ${throwable.message}")
                        view?.displayErrorView()
                    }
                })
    }

    override fun onTeacherLike(teacherId: Int, isLiked: Boolean) {
        if (isLiked) {
            subscriptionApiManager.addTeacherSubscription(teacherId)
        } else {
            subscriptionApiManager.removeTeacherSubscription(teacherId)
        }
        AnalyticsHelper.setUserProperty(USER_PROPERTY_NUMBER_FAVORITE_TEACHERS, AppPreferences.numberFavoriteTeachers())
        view?.onFavoriteClicked(AppPreferences.hasFavoriteTeacher(teacherId))
    }

    override fun findTeacherOnYouTube(teacher: Teacher) {
        Timber.d("Searching for ${teacher.name} on YouTube")
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