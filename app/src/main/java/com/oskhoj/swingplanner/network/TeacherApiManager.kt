package com.oskhoj.swingplanner.network

import com.oskhoj.swingplanner.network.service.TeacherService

class TeacherApiManager(private val teacherService: TeacherService) {

    fun allTeachers() = teacherService.findAllTeachers()

    fun searchTeachers(query: String) = teacherService.searchTeachers(query)

    fun eventsByTeacher(teacherId: Int) = teacherService.eventsByTeacher(teacherId)
}