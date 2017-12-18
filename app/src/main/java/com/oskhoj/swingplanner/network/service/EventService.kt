package com.oskhoj.swingplanner.network.service

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventService {

    @GET("event/")
    fun searchEvents(@Query("q") q: String, @Query("styles") styles: String, @Query("p") p: Int): Single<ResponseBody>

    @GET("event/list/")
    fun eventsByIds(@Query("ids") eventIds: String): Single<ResponseBody>

    @GET("event/details/{eventId}")
    fun eventDetailsById(@Path("eventId") eventId: Int): Single<ResponseBody>
}