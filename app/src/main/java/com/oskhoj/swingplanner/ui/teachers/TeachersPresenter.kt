package com.oskhoj.swingplanner.ui.teachers

import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.Teacher
import com.oskhoj.swingplanner.network.EventApiManager
import com.oskhoj.swingplanner.network.TeacherApiManager
import com.oskhoj.swingplanner.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class TeachersPresenter(private val eventsApiManager: EventApiManager, private val teacherApiManager: TeacherApiManager) : BasePresenter<TeachersContract.View>(), TeachersContract.Presenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun loadTeachers(query: String) {
        val teachers = listOf(Teacher(1, "Skye Humphries"), Teacher(2, "Fredrik Dahlberg"), Teacher(3, "Mimmi Gunnarsson"), Teacher(4, "Frida Segerdahl")
                , Teacher(5, "Anders Sahlberg"), Teacher(6, "Vicci Moore"), Teacher(7, "Gaston Fernandez"), Teacher(8, "Alba Mengual"), Teacher(9, "Nils Petterson"),
                Teacher(10, "Larissa Conrad"), Teacher(11, "Andreas Johansson"), Teacher(5, "ANDERS JOHANSON"))
        view?.displayTeachers(teachers.filter { it.name.contains(query.trim(), true) })

//        teacherApiManager.allTeachers(
//                object : Callback<List<Teacher>> {
//                    override fun onFailure(call: Call<List<Teacher>>?, t: Throwable?) {
//                        Timber.w(t, "Request failed")
//                        view?.displayErrorView()
//                    }
//
//                    override fun onResponse(call: Call<List<Teacher>>, response: Response<List<Teacher>>) {
//                        if (response.isSuccessful) {
//                            val teachers = response.body() ?: emptyList()
//                            Timber.d("Request succeeded, got ${teachers.size} teachers")
//                            view?.displayTeachers(teachers.filter { it.name.contains(query.trim(), true) })
//                        } else {
//                            Timber.d("Failed to execute request, ${response.errorBody()}")
//                            view?.displayErrorView()
//                        }
//                    }
//                })
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