package com.oskhoj.swingplanner.model

import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@PaperParcel
data class Teacher(
        val id: Int,
        val name: String) : PaperParcelable, Comparable<Teacher> {
    
    override fun compareTo(other: Teacher): Int {
        return name.compareTo(other.name, true)
    }

    companion object {
        @JvmField
        val CREATOR = PaperParcelTeacher.CREATOR
    }
}