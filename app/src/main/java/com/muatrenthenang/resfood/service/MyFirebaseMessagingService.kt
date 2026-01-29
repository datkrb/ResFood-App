package com.muatrenthenang.resfood.service

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.muatrenthenang.resfood.util.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // TODO: Send this token to your backend server if you have one, 
        // to target this specific device for notifications.
        // For now, we just log it.
        getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if push notifications are enabled
        val prefs = getSharedPreferences("resfood_prefs", Context.MODE_PRIVATE)
        val isPushEnabled = prefs.getBoolean("push_notification_enabled", true)
        
        if (!isPushEnabled) {
            Log.d("FCM", "Push notifications disabled by user, ignoring message")
            return
        }

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            // If the app is in the foreground, Firebase doesn't automatically show the notification
            // (unless you configure it to). We can show it manually.
            // Note: If you want unified behavior, you might parse the payload and use NotificationHelper.
            
            // Simple generic notification for now:
            // You might want to customize this based on title/body
             NotificationHelper.showOrderStatusNotification(
                 this, 
                 "System", 
                 it.body ?: "New Message"
             )
        }
    }
    
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        val orderId = data["orderId"]
        
        if (type == "order_update" && orderId != null) {
            val status = data["status"] ?: "Updated"
            NotificationHelper.showOrderStatusNotification(this, orderId, status)
        } else if (type == "new_order" && orderId != null) {
             val total = data["total"]?.toIntOrNull() ?: 0
             NotificationHelper.showNewOrderNotification(this, orderId, total)
        } else if (type == "promo" || type == "promotion") {
            val title = data["title"] ?: "Khuyến mãi mới!"
            val body = data["body"] ?: "Xem ngay các ưu đãi hấp dẫn dành cho bạn."
            NotificationHelper.showPromotionNotification(this, title, body)
        }
    }
}
