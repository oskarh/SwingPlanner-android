package com.oskhoj.swingplanner.ui.teachers

import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.Teacher
import com.oskhoj.swingplanner.ui.base.Attachable
import com.oskhoj.swingplanner.ui.base.BaseView

object TeachersContract {

    interface View : BaseView {
        fun displayTeachers(teachers: List<Teacher>)

        fun abortSearch()

        fun clearText()

        fun openTeacherDetails(events: List<EventSummary>)

        fun displayEmptyView()

        fun displayErrorView()

        fun onFavoriteClicked(isFavorite: Boolean)

        fun openEventDetails(eventSummary: EventSummary, eventDetails: EventDetails)
    }

    interface Presenter : Attachable<View> {
        fun loadTeachers(query: String = "")

        fun onSearchBack()

        fun onSearchClear()

        fun openTeacherDetails(teacher: Teacher)

        fun toggleTeacherLike(teacher: Teacher)

        fun findTeacherOnYouTube(teacher: Teacher)

        fun openEventDetails(eventSummary: EventSummary)

        fun aboutAction()
    }
}