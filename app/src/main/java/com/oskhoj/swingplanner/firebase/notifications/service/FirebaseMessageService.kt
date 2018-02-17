package com.oskhoj.swingplanner.firebase.notifications.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.oskhoj.swingplanner.firebase.notifications.NotificationHelper
import com.oskhoj.swingplanner.firebase.notifications.NotificationType
import com.oskhoj.swingplanner.util.CHANNEL
import timber.log.Timber

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        Timber.d("Received message: ${remoteMessage?.data?.get("channel")} ${remoteMessage?.notification?.title} ${remoteMessage?.notification?.body}")
        val title = remoteMessage?.data?.get("title") ?: ""
        val body = remoteMessage?.data?.get("body") ?: ""
        val id: Int = remoteMessage?.data?.get("id")?.toIntOrNull() ?: 0
        val notificationHelper = NotificationHelper(this)
        val channel = remoteMessage?.data?.get(CHANNEL)
        when (channel) {
            NotificationType.TEACHER.channelName -> notificationHelper.notify(title, body, id, NotificationType.TEACHER)
            NotificationType.EVENT.channelName -> notificationHelper.notify(title, body, id, NotificationType.EVENT)
            NotificationType.MISCELLANEOUS.channelName -> notificationHelper.notify(title, body, id, NotificationType.MISCELLANEOUS)
            else -> Timber.d("Ignored unknown channel $channel")
        }
    }
}