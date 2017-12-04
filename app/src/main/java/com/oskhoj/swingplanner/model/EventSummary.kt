package com.oskhoj.swingplanner.model

import com.oskhoj.swingplanner.util.SimpleDate
import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@PaperParcel
data class EventSummary(
        val id: Int,
        val eventDetailsId: Int,
        val name: String,
        val city: String,
        val country: Country,
        val startDay: String,
        val endDay: String?,
        val imageUrl: String?) : PaperParcelable, Comparable<EventSummary> {

    val startDate: SimpleDate
        get() = SimpleDate.from(startDay)

    val endDate: SimpleDate
        get() = endDay?.let {
            SimpleDate.from(it)
        } ?: startDate

    val dayOfMonth: String
        get() = startDate.dayOfMonth.toString()

    fun isOneDayEvent() = startDate == endDate

    override fun compareTo(other: EventSummary) =
            startDate.compareTo(other.startDate).takeUnless { startDate == other.startDate }
                    ?: name.compareTo(other.name, true)

    companion object {
        @JvmField
        val CREATOR = PaperParcelEventSummary.CREATOR
    }
}