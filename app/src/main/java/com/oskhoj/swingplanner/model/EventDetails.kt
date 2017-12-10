package com.oskhoj.swingplanner.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@SuppressLint("ParcelCreator")
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
        val teachersDescription: String?) : Parcelable