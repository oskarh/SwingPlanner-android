package com.oskhoj.swingplanner.network.service

import com.oskhoj.swingplanner.model.EventDetails
import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.model.EventsPage
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventService {

    @GET("event/")
    fun searchEvents(@Query("q") q: String, @Query("styles") styles: String): Single<EventsPage>

    @GET("event/list/")
    fun eventsByIds(@Query("ids") eventIds: String): Single<List<EventSummary>>

    @GET("event/details/{eventId}")
    fun eventDetailsById(@Path("eventId") eventId: Int): Single<EventDetails>
}