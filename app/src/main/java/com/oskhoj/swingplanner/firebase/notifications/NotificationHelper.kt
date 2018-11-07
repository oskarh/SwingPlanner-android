package com.oskhoj.swingplanner.firebase.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.oskhoj.swingplanner.MainActivity
import com.oskhoj.swingplanner.R
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper
import com.oskhoj.swingplanner.util.KEY_STATE_DEEP_LINK_EVENT_ID

class NotificationHelper(context_: Context) {
    private val context: Context = context_.applicationContext
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun notify(title: String, body: String, eventId: Int, notificationType: NotificationType) {
        AnalyticsHelper.logEvent(notificationType)
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(KEY_STATE_DEEP_LINK_EVENT_ID, eventId)
        }
        val pendingIntent = PendingIntent.getActivity(context, eventId, intent, PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(context, notificationType.channelName)
        notification.setContentTitle(getTitle(notificationType, title))
                .setContentText(getBody(notificationType, body))
                .setSmallIcon(notificationType.notificationIcon)
                .setAutoCancel(true)
//                .setGroup(notificationType.channelName)
                .setContentIntent(pendingIntent)
        notificationManager.notify(notificationType.id + eventId, notification.build())
    }

    private fun getTitle(notificationType: NotificationType, parameter: String): String {
        return when (notificationType) {
            NotificationType.CUSTOM_QUERY -> context.getString(R.string.notification_custom_query_title, parameter)
            NotificationType.EVENT -> context.getString(R.string.notification_event_title, parameter)
            NotificationType.MISCELLANEOUS -> parameter
            NotificationType.TEACHER -> context.getString(R.string.notification_teacher_update_title, parameter)
        }
    }

    private fun getBody(notificationType: NotificationType, parameter: String): String {
        return when (notificationType) {
            NotificationType.CUSTOM_QUERY -> parameter
            NotificationType.EVENT -> context.getString(R.string.notification_event_body, parameter)
            NotificationType.MISCELLANEOUS -> parameter
            NotificationType.TEACHER -> context.getString(R.string.notification_teacher_update_body, parameter)
        }
    }
}