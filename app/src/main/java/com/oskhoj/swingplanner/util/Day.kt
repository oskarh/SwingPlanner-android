package com.oskhoj.swingplanner.util

import android.content.Context
import com.oskhoj.swingplanner.R
import java.util.Calendar

object Day {

    fun getDay(simpleDate: SimpleDate, context: Context): String {
        val dayOfWeek = simpleDate.toCalendar().get(Calendar.DAY_OF_WEEK)
        return translateDay(dayOfWeek, context)
    }

    private fun translateDay(dayOfWeek: Int, context: Context) = when (dayOfWeek) {
        Calendar.SUNDAY -> context.getString(R.string.sunday)
        Calendar.MONDAY -> context.getString(R.string.monday)
        Calendar.TUESDAY -> context.getString(R.string.tuesday)
        Calendar.WEDNESDAY -> context.getString(R.string.wednesday)
        Calendar.THURSDAY -> context.getString(R.string.thursday)
        Calendar.FRIDAY -> context.getString(R.string.friday)
        Calendar.SATURDAY -> context.getString(R.string.saturday)
        else -> throw IllegalArgumentException("Incorrect day of week [$dayOfWeek], must be between ${Calendar.SUNDAY} and ${Calendar.SATURDAY}")
    }
}