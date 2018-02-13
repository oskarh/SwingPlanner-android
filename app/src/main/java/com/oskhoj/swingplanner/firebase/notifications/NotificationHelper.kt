package com.oskhoj.swingplanner.firebase.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.oskhoj.swingplanner.firebase.analytics.AnalyticsHelper

class NotificationHelper(context_: Context) {
    private val context: Context = context_.applicationContext
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationType.values().forEach {
                notificationManager.createNotificationChannel(
                        NotificationChannel(it.channelName, context.getString(it.stringRes), NotificationManager.IMPORTANCE_DEFAULT))
            }
        }
    }

    fun notify(title: String, body: String, notificationType: NotificationType) {
        AnalyticsHelper.logEvent(notificationType)
        val notification =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Notification.Builder(context, notificationType.channelName)
                } else {
                    Notification.Builder(context)
                }
        notification.setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(notificationType.notificationIcon)
                .setAutoCancel(true)
        notificationManager.notify(notificationType.id, notification.build())
    }
}