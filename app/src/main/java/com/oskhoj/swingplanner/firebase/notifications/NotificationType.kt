package com.oskhoj.swingplanner.firebase.notifications

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.oskhoj.swingplanner.R

enum class NotificationType(val channelName: String, val id: Int, @StringRes val stringRes: Int, @DrawableRes val notificationIcon: Int) {
    EVENT("event_channel", 1000, R.string.channel_event, R.drawable.dancing_icon),
    TEACHER("teacher_channel", 100_000, R.string.channel_teacher, R.drawable.dancing_icon),
    CUSTOM_QUERY("custom_channel", 100_000, R.string.channel_custom, R.drawable.dancing_icon),
    MISCELLANEOUS("miscellaneous_channel", 1, R.string.channel_miscellaneous, R.drawable.dancing_icon)
}