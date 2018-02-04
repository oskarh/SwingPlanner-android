package com.oskhoj.swingplanner.notifications.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        Timber.d("Received message: ${remoteMessage?.data?.get("title")} ${remoteMessage?.notification?.title} ${remoteMessage?.notification?.body}")
    }
}