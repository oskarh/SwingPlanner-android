package com.oskhoj.swingplanner.network

import com.oskhoj.swingplanner.network.service.EventService

class EventApiManager(private val eventService: EventService) {

    fun searchEvents(eventSearchParams: EventSearchParams) = eventService.searchEvents(eventSearchParams.query, eventSearchParams.danceStyles(), eventSearchParams.page)

    fun eventsByIds(eventIds: List<Int>) = eventService.eventsByIds(eventIds.joinToString(","))

    fun eventDetailsById(eventId: Int) = eventService.eventDetailsById(eventId)
}