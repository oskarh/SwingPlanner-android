package com.oskhoj.swingplanner.firebase.notifications

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.oskhoj.swingplanner.R

// TODO: Change to correct notification icons
enum class NotificationType(val channelName: String, val id: Int, @StringRes val stringRes: Int, @DrawableRes val notificationIcon: Int) {
    EVENT("event_channel", 1000, R.string.event_channel, R.drawable.dancing_icon),
    TEACHER("teacher_channel", 100_000, R.string.teacher_channel, R.drawable.dancing_icon),
    MISCELLANEOUS("miscellaneous_channel", 1, R.string.miscellaneous_channel, R.drawable.dancing_icon)
}