package com.oskhoj.swingplanner.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@SuppressLint("ParcelCreator")
data class EventsPage(
        @SerializedName("content")
        val events: List<EventSummary>,
        @SerializedName("number")
        val pageNumber: Int,
        @SerializedName("first")
        val isFirstPage: Boolean,
        @SerializedName("last")
        val isLastPage: Boolean,
        val totalPages: Int,
        val totalElements: Int,
        val numberOfElements: Int,
        val size: Int) : Parcelable {

    fun hasNoEvents() = totalElements == 0
}
