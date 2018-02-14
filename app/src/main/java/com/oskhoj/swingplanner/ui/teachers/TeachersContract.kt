package com.oskhoj.swingplanner.ui.teachers

import com.oskhoj.swingplanner.model.Teacher
import com.oskhoj.swingplanner.model.TeacherEventsResponse
import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView
import com.oskhoj.swingplanner.ui.base.Loadable

object TeachersContract {

    interface View : BaseView, Loadable {
        fun displayTeachers(teachers: List<Teacher>)

        fun displayTeacherEvents(teacherEventsResponse: TeacherEventsResponse)

        fun abortSearch()

        fun clearText()

        fun displayEmptyView()

        fun displayErrorView()

        fun onFavoriteClicked(isFavorite: Boolean)
    }

    interface Presenter : Attachable<View> {
        fun loadTeachers(query: String = "")

        fun onSearchBack()

        fun onSearchClear()

        fun openTeacherDetails(teacher: Teacher)

        fun toggleTeacherLike(teacher: Teacher)

        fun findTeacherOnYouTube(teacher: Teacher)

        fun aboutAction()
    }
}