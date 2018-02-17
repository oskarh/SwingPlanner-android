package com.oskhoj.swingplanner.firebase.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.oskhoj.swingplanner.MainActivity
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

    fun notify(title: String, body: String, eventId: Int, notificationType: NotificationType) {
        AnalyticsHelper.logEvent(notificationType)
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(context, notificationType.channelName)
        notification.setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(notificationType.notificationIcon)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
        notificationManager.notify(notificationType.id + eventId, notification.build())
    }
}