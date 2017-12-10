package com.oskhoj.swingplanner.network.service

import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.Teacher
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface TeacherService {

    @GET("teacher/")
    fun findAllTeachers(): Single<List<Teacher>>

    @GET("teacher/{query}")
    fun searchTeachers(@Path("query") query: String): Single<List<Teacher>>

    @GET("teacher/{teacherId}/events")
    fun eventsByTeacher(@Path("teacherId") teacherId: Int): Single<List<EventSummary>>
}
