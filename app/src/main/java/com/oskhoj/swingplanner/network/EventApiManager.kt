package com.oskhoj.swingplanner.network

import com.oskhoj.swingplanner.network.service.EventService
import timber.log.Timber

class EventApiManager(private val eventService: EventService) {

    init {
        Timber.d("Creating EventApiManager...")
    }

    fun searchEvents(query: CharSequence, styles: String) = eventService.searchEvents(query.toString(), styles)

    fun eventsByIds(eventIds: List<Int>) = eventService.eventsByIds(eventIds.joinToString(","))

    fun eventDetailsById(eventId: Int) = eventService.eventDetailsById(eventId)
}