package com.oskhoj.swingplanner.network

import com.oskhoj.swingplanner.AppPreferences
import com.oskhoj.swingplanner.network.service.SubscriptionService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class SubscriptionApiManager(private val subscriptionService: SubscriptionService) {

    private val statusCallbackLogger = object : Callback<Unit> {
        override fun onResponse(call: Call<Unit>?, response: Response<Unit>) {
            if (response.isSuccessful) {
                Timber.d("Subscription added in backend")
            } else {
                Timber.d("Failed to add subscription")
            }
        }

        override fun onFailure(call: Call<Unit>?, t: Throwable?) {
            Timber.d("Failed to add subscription")
        }
    }

    fun addEventSubscription(eventId: Int) {
        subscriptionService.addEventSubscription(AppPreferences.firebaseToken, eventId)
                .enqueue(statusCallbackLogger)
    }

    fun removeEventSubscription(eventId: Int) {
        subscriptionService.removeEventSubscription(AppPreferences.firebaseToken, eventId)
                .enqueue(statusCallbackLogger)
    }

    fun addTeacherSubscription(teacherId: Int) {
        subscriptionService.addTeacherSubscription(AppPreferences.firebaseToken, teacherId)
                .enqueue(statusCallbackLogger)
    }

    fun removeTeacherSubscription(teacherId: Int) {
        subscriptionService.removeTeacherSubscription(AppPreferences.firebaseToken, teacherId)
                .enqueue(statusCallbackLogger)
    }

    fun addCustomSubscription(query: String) {
        subscriptionService.addCustomSubscription(AppPreferences.firebaseToken, query)
                .enqueue(statusCallbackLogger)
    }

    fun removeCustomSubscription(query: String) {
        subscriptionService.removeCustomSubscription(AppPreferences.firebaseToken, query)
                .enqueue(statusCallbackLogger)
    }
}