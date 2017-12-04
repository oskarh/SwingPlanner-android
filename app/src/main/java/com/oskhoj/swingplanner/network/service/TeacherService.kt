package com.oskhoj.swingplanner.network.service

import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.Teacher
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface TeacherService {

    @GET("teacher/")
    fun findAllTeachers(): Call<List<Teacher>>

    @GET("teacher/{query}")
    fun searchTeachers(@Path("query") query: String): Call<List<Teacher>>

    @GET("teacher/{teacherId}/events")
    fun eventsByTeacher(@Path("teacherId") teacherId: Int): Call<List<EventSummary>>
}
