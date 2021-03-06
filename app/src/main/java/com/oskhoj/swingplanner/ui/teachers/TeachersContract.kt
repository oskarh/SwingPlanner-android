package com.oskhoj.swingplanner.ui.teachers

import com.oskhoj.swingplanner.model.Teacher
import com.oskhoj.swingplanner.model.TeacherEventsResponse
import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView

object TeachersContract {

    interface View : BaseView {
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

        fun openTeacherDetails(teacherId: Int)

        fun onTeacherLike(teacherId: Int, isLiked: Boolean)

        fun findTeacherOnYouTube(teacher: Teacher)

        fun aboutAction()
    }
}