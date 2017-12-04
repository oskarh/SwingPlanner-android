package com.oskhoj.swingplanner.network

import com.oskhoj.swingplanner.model.EventSummary
import com.oskhoj.swingplanner.network.service.EventService
import io.reactivex.Single
import timber.log.Timber

class EventApiManager(private val eventService: EventService) {

    init {
        Timber.d("Creating EventApiManager...")
    }

    fun allEvents(): Single<List<EventSummary>> =
            eventService.findAllEvents()

    fun searchEvents(query: String) =
            eventService.searchEvents(query)

    fun eventsByIds(eventIds: List<Int>) =
            eventService.eventsByIds(eventIds.joinToString(","))

    fun eventDetailsById(eventId: Int) =
            eventService.eventDetailsById(eventId)
}