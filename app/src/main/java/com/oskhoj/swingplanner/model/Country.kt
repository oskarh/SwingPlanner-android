package com.oskhoj.swingplanner.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@SuppressLint("ParcelCreator")
data class Country(
        val name: String,
        val isoCode: String?) : Parcelable