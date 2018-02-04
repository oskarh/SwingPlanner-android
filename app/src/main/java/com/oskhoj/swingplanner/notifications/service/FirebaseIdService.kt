package com.oskhoj.swingplanner.notifications.service

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import timber.log.Timber

class FirebaseIdService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        super.onTokenRefresh()
        val token = FirebaseInstanceId.getInstance().token
        Timber.d("New Firebase token: [$token]")
        token?.let {
            sendRegistrationToServer(it)
        }
    }

    private fun sendRegistrationToServer(token: String) {
        // TODO: Send token to server
    }
}
