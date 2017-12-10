package com.oskhoj.swingplanner.network

import com.oskhoj.swingplanner.network.service.TeacherService
import timber.log.Timber

class TeacherApiManager(private val teacherService: TeacherService) {

    init {
        Timber.d("Creating TeacherApiManager...")
    }

    fun allTeachers() = teacherService.findAllTeachers()

    fun searchTeachers(query: String) = teacherService.searchTeachers(query)

    fun eventsByTeacher(teacherId: Int) = teacherService.eventsByTeacher(teacherId)
}