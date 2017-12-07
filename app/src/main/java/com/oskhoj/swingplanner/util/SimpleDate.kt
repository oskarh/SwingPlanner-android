package com.oskhoj.swingplanner.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

data class SimpleDate(val year: Int, val month: Int, val dayOfMonth: Int) : Comparable<SimpleDate> {

    val europeanFormat: String
        get() = "$year$DATE_SEPARATOR${month.format()}$DATE_SEPARATOR${dayOfMonth.format()}"

    val usFormat: String
        get() = "$year$DATE_SEPARATOR${dayOfMonth.format()}$DATE_SEPARATOR${month.format()}"

    private fun Int.format() = toString().padStart(2, '0')

    override fun compareTo(other: SimpleDate) = when {
        year != other.year -> year.compareTo(other.year)
        month != other.month -> month.compareTo(other.month)
        else -> dayOfMonth.compareTo(other.dayOfMonth)
    }

    fun getShortMonth(context: Context) = Month.getMonth(month, context).take(3)

    fun getShortDay(context: Context) = Day.getDay(this, context).take(3)

    fun toCalendar(): Calendar {
        val calendar: Calendar = GregorianCalendar(Locale.ENGLISH)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        return calendar
    }

    companion object {
        fun from(dateString: String): SimpleDate {
            try {
                dateFormat.parse(dateString)
            } catch (e: Exception) {
                throw IllegalArgumentException("Incorrect date format for SimpleDate have to be [yyyy${DATE_SEPARATOR}MM${DATE_SEPARATOR}dd], was [$dateString]")
            }
            val fields = dateString.split(DATE_SEPARATOR)

            return SimpleDate(fields[0].toInt(), fields[1].toInt(), fields[2].toInt())
        }

        private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy${DATE_SEPARATOR}MM${DATE_SEPARATOR}dd")
    }
}