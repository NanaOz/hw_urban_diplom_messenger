package com.example.hw_urban_diplom_messenger.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class PushNotificationService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMToken", token)
    }
}
