package com.oskhoj.swingplanner.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Locale

@Parcelize
@SuppressLint("ParcelCreator")
data class SearchEventsPage(private val _query: String, val eventsPage: EventsPage) : Parcelable {

    constructor(query: CharSequence, eventsPage: EventsPage) : this(query.toString(), eventsPage)

    val query: String = _query
        get() = field.trim().toLowerCase(Locale.ENGLISH)

    val events = eventsPage.events

    val hasNextPage = !eventsPage.isLastPage
}
