package com.oskhoj.swingplanner.model

import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@PaperParcel
data class Country(
        val name: String,
        val isoCode: String?) : PaperParcelable {
    companion object {
        @JvmField
        val CREATOR = PaperParcelCountry.CREATOR
    }
}