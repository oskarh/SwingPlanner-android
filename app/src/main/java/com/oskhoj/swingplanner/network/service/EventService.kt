package com.oskhoj.swingplanner.network.service

import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface EventService {

    //    @GET("event/search/stockholm")
    @GET("event/")
    fun findAllEvents(): Single<List<EventSummary>>

    @GET("event/search/{query}")
    fun searchEvents(@Path("query") query: String): Single<List<EventSummary>>

    @GET("event/list/{eventIds}")
    fun eventsByIds(@Path("eventIds") eventIds: String): Single<List<EventSummary>>

    @GET("event/details/{eventId}")
    fun eventDetailsById(@Path("eventId") eventId: Int): Single<EventDetails>
}