package com.oskhoj.swingplanner.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Country(
        val name: String,
        val isoCode: String?) : Parcelable