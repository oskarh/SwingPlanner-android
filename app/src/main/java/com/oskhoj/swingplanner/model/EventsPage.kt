package com.oskhoj.swingplanner.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.oskhoj.swingplanner.util.DanceStyle
import com.oskhoj.swingplanner.util.enumSetFrom
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

@Parcelize
@SuppressLint("ParcelCreator")
data class EventsPage(
        val query: String,
        val stylesFilter: String,
        @SerializedName("page")
        val pageNumber: Int,
        val pageSize: Int,
        val pageCount: Int,
        val totalEvents: Int,
        @SerializedName("lastPage")
        val isLastPage: Boolean,
        @SerializedName("pageList")
        val events: List<EventSummary>) : Parcelable {

    fun hasNoEvents() = totalEvents == 0

    @Transient
    val hasNextPage = !isLastPage

    @Transient
    var stylesFilterSet = enumSetFrom<DanceStyle>(stylesFilter)
        get() = enumSetFrom<DanceStyle>(stylesFilter)
        private set

    fun isSameSearchNextPage(other: EventsPage): Boolean {
        Timber.d("Comparing $this to $other")
        return query.equals(other.query, true) && stylesFilterSet == other.stylesFilterSet && pageNumber < other.pageNumber
    }

    fun EventsPage?.hasEvents() = this != null && totalEvents > 0
}
