package com.oskhoj.swingplanner.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@SuppressLint("ParcelCreator")
data class Teacher(
        val id: Int,
        val name: String) : Parcelable, Comparable<Teacher> {
    
    override fun compareTo(other: Teacher): Int {
        return name.compareTo(other.name, true)
    }
}