package com.oskhoj.swingplanner.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.oskhoj.swingplanner.util.SimpleDate
import kotlinx.android.parcel.Parcelize

@Parcelize
@SuppressLint("ParcelCreator")
data class EventSummary(
        val id: Int,
        val eventDetailsId: Int,
        val name: String,
        val city: String,
        val country: Country,
        private val startDay: String,
        private val endDay: String?,
        val imageUrl: String?) : Parcelable, Comparable<EventSummary> {

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
}