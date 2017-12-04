package com.oskhoj.swingplanner.network

import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.Teacher
import com.oskhoj.swingplanner.network.service.TeacherService
import retrofit2.Callback
import timber.log.Timber

class TeacherApiManager(private val teacherService: TeacherService) {

    init {
        Timber.d("Creating TeacherApiManager...")
    }

    fun allTeachers(callback: Callback<List<Teacher>>) =
            teacherService.findAllTeachers()
                    .enqueue(callback)

    fun searchTeachers(query: String, callback: Callback<List<Teacher>>) =
            teacherService.searchTeachers(query)
                    .enqueue(callback)

    fun eventsByTeacher(teacherId: Int, callback: Callback<List<EventSummary>>) =
            teacherService.eventsByTeacher(teacherId)
                    .enqueue(callback)
}