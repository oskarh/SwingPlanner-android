package com.oskhoj.swingplanner.network.service

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface SubscriptionService {

    @POST("subscription/event/{token}/{eventId}")
    fun addEventSubscription(@Path("token") token: String, @Path("eventId") eventId: Int): Call<Unit>

    @DELETE("subscription/event/{token}/{eventId}")
    fun removeEventSubscription(@Path("token") token: String, @Path("eventId") eventId: Int): Call<Unit>

    @POST("subscription/teacher/{token}/{teacherId}")
    fun addTeacherSubscription(@Path("token") token: String, @Path("teacherId") teacherId: Int): Call<Unit>

    @DELETE("subscription/teacher/{token}/{teacherId}")
    fun removeTeacherSubscription(@Path("token") token: String, @Path("teacherId") teacherId: Int): Call<Unit>

    @POST("subscription/custom/{token}/{query}")
    fun addCustomSubscription(@Path("token") token: String, @Path("query") query: String): Call<Unit>

    @DELETE("subscription/custom/{token}/{query}")
    fun removeCustomSubscription(@Path("token") token: String, @Path("query") query: String): Call<Unit>
}