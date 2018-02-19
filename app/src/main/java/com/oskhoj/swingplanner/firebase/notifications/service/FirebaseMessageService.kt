package com.oskhoj.swingplanner.firebase.notifications.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.oskhoj.swingplanner.firebase.notifications.NotificationHelper
import com.oskhoj.swingplanner.firebase.notifications.NotificationType
import com.oskhoj.swingplanner.util.BODY
import com.oskhoj.swingplanner.util.CHANNEL
import com.oskhoj.swingplanner.util.ID
import com.oskhoj.swingplanner.util.TITLE
import timber.log.Timber

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        val channel = remoteMessage?.data?.get(CHANNEL)
        val id = remoteMessage?.data?.get(ID)?.toIntOrNull() ?: 0
        val title = remoteMessage?.data?.get(TITLE) ?: ""
        val body = remoteMessage?.data?.get(BODY) ?: ""
        Timber.d("Received message: $channel $id")
        val notificationHelper = NotificationHelper(this)
        when (channel) {
            NotificationType.CUSTOM_QUERY.channelName -> notificationHelper.notify(title, body, id, NotificationType.CUSTOM_QUERY)
            NotificationType.EVENT.channelName -> notificationHelper.notify(title, body, id, NotificationType.EVENT)
            NotificationType.MISCELLANEOUS.channelName -> notificationHelper.notify(title, body, id, NotificationType.MISCELLANEOUS)
            NotificationType.TEACHER.channelName -> notificationHelper.notify(title, body, id, NotificationType.TEACHER)
            else -> Timber.d("Ignored unknown channel $channel")
        }
    }
}