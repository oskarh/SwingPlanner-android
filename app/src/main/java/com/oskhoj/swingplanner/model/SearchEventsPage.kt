package com.oskhoj.swingplanner.model

import java.util.Locale

data class SearchEventsPage(private val _query: String, val eventsPage: EventsPage) {

    constructor(query: CharSequence, eventsPage: EventsPage) : this(query.toString(), eventsPage)

    val query: String = _query
        get() = field.trim().toLowerCase(Locale.ENGLISH)

    val events = eventsPage.events
}
