package com.oskhoj.swingplanner.util

import android.content.Context
import android.support.annotation.StringRes
import com.oskhoj.swingplanner.R

enum class Month(@StringRes private val stringRes: Int) {
    JANUARY(R.string.january),
    FEBRUARY(R.string.february),
    MARCH(R.string.march),
    APRIL(R.string.april),
    MAY(R.string.may),
    JUNE(R.string.june),
    JULY(R.string.july),
    AUGUST(R.string.august),
    SEPTEMBER(R.string.september),
    OCTOBER(R.string.october),
    NOVEMBER(R.string.november),
    DECEMBER(R.string.december);

    companion object {
        fun getMonth(ordinal: Int, context: Context): String {
            if (ordinal !in 1..12) {
                throw IllegalArgumentException("$ordinal is out of range for months, should be an integer between 1 and 12")
            }
            return context.getString(Month.values()[ordinal - 1].stringRes)
        }

        fun getShortMonth(ordinal: Int, context: Context) = getMonth(ordinal, context).take(3)
    }
}