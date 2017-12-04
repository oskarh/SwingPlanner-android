package com.oskhoj.swingplanner.model

import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@PaperParcel
data class EventDetails(
        val id: Int,
        val description: String,
        val website: String?,
        val facebookEventUrl: String?,
        val attendanceCount: Int?,
        val interestedCount: Int?,
        val competitionsText: String?,
        val danceStyles: String,
        val teachers: List<Teacher>?,
        val teachersDescription: String?) : PaperParcelable {
    companion object {
        @JvmField
        val CREATOR = PaperParcelEventDetails.CREATOR
    }
}
