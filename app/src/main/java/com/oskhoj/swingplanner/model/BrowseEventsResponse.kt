package com.oskhoj.swingplanner.model

import com.google.gson.annotations.SerializedName

data class BrowseEventsResponse(
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
        val size: Int)
